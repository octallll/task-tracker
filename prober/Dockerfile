FROM python:3.13-alpine
RUN pip install --no-cache-dir prometheus-client==0.26.0 \
    && adduser -S prober
WORKDIR /app
COPY prober.py .
USER prober
ENV PYTHONUNBUFFERED=1
EXPOSE 9100
ENTRYPOINT ["python", "prober.py"]
