FROM postgres:15
RUN apt-get update && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*
COPY server.py /server.py
RUN useradd -m backupsvc && mkdir -p /backups && chown backupsvc /backups
USER backupsvc
CMD ["python3", "/server.py"]