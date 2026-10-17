--liquibase formatted sql

--changeset task-tracker:001-create-boards
CREATE TABLE boards
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

--changeset task-tracker:002-create-tags
CREATE TABLE tags
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(7)  NOT NULL DEFAULT '#6b7280'
);

--changeset task-tracker:003-create-tasks
CREATE TABLE tasks
(
    id          BIGSERIAL PRIMARY KEY,
    board_id    BIGINT       NOT NULL REFERENCES boards (id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(4000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO'
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    due_date    DATE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_board_status ON tasks (board_id, status);

--changeset task-tracker:004-create-task-tags
CREATE TABLE task_tags
(
    task_id BIGINT NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);
