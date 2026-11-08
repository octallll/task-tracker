import json
import os
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone

from prometheus_client import Counter, Gauge, Histogram, start_http_server

TARGET = os.environ.get("PROBER_TARGET", "http://frontend").rstrip("/")
INTERVAL = float(os.environ.get("PROBER_INTERVAL_SECONDS", "15"))
TIMEOUT = float(os.environ.get("PROBER_TIMEOUT_SECONDS", "2"))
PORT = int(os.environ.get("PROBER_PORT", "9100"))

RUNS = Counter("prober_runs", "Prober scenario runs", ["result"])
UP = Gauge("prober_up", "1 if the last scenario run succeeded, 0 otherwise")
RUN_DURATION = Gauge("prober_run_duration_seconds", "Duration of the last scenario run")
STEP_SUCCESS = Gauge("prober_step_success", "1 if the step succeeded in the last run", ["step"])
STEP_DURATION = Histogram(
    "prober_step_duration_seconds",
    "Duration of a scenario step",
    ["step"],
    buckets=(0.05, 0.1, 0.3, 0.5, 1, 2),
)


class StepFailed(Exception):
    pass


def log(level, message, **fields):
    record = {
        "@timestamp": datetime.now(timezone.utc).isoformat(),
        "log.level": level,
        "service.name": "task-tracker-prober",
        "message": message,
        **fields,
    }
    print(json.dumps(record, ensure_ascii=False), flush=True)


def call(method, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(TARGET + path, data=data, method=method)
    if data is not None:
        request.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT) as response:
            payload = response.read()
            return json.loads(payload) if payload and path.startswith("/api/") else None
    except urllib.error.HTTPError as e:
        raise StepFailed(f"{method} {path} -> {e.code}") from e
    except (urllib.error.URLError, TimeoutError, OSError) as e:
        raise StepFailed(f"{method} {path} -> {e}") from e


def step(name, method, path, body=None):
    started = time.monotonic()
    try:
        result = call(method, path, body)
        STEP_SUCCESS.labels(name).set(1)
        return result
    except StepFailed:
        STEP_SUCCESS.labels(name).set(0)
        raise
    finally:
        STEP_DURATION.labels(name).observe(time.monotonic() - started)


def run_scenario():
    board_id = None
    try:
        step("healthz", "GET", "/healthz")
        step("list_boards", "GET", "/api/boards")
        board = step("create_board", "POST", "/api/boards", {"name": f"prober-{int(time.time())}"})
        board_id = board["id"]
        task = step("create_task", "POST", f"/api/boards/{board_id}/tasks", {"title": "prober task"})
        step("complete_task", "PATCH", f"/api/tasks/{task['id']}/status", {"status": "DONE"})
        step("delete_board", "DELETE", f"/api/boards/{board_id}")
        board_id = None
    finally:
        if board_id is not None:
            try:
                call("DELETE", f"/api/boards/{board_id}")
            except StepFailed:
                pass


def main():
    start_http_server(PORT)
    log("INFO", "Prober started", target=TARGET, interval_seconds=INTERVAL)
    for name in ("healthz", "list_boards", "create_board", "create_task", "complete_task", "delete_board"):
        STEP_SUCCESS.labels(name)
        STEP_DURATION.labels(name)
    while True:
        started = time.monotonic()
        try:
            run_scenario()
            UP.set(1)
            RUNS.labels("success").inc()
        except (StepFailed, KeyError, TypeError, ValueError) as e:
            UP.set(0)
            RUNS.labels("failure").inc()
            log("WARN", "Probe failed", error=str(e))
        duration = time.monotonic() - started
        RUN_DURATION.set(duration)
        time.sleep(max(0.0, INTERVAL - duration))


if __name__ == "__main__":
    main()
