import json, os, subprocess, time, hmac
from http.server import BaseHTTPRequestHandler, HTTPServer

TOKEN = os.environ["BACKUP_TOKEN"]
H, U, DB = os.environ["PGHOST"], os.environ["PGUSER"], os.environ["PGDATABASE"]
DIR = "/backups"


def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True)


def count(db):
    r = sh(f'psql -h {H} -U {U} -d {db} -t -A -c "SELECT count(*) FROM products"')
    return int(r.stdout.strip()) if r.returncode == 0 and r.stdout.strip() else -1


def do_backup():
    f = f"{DIR}/backup_{time.strftime('%Y%m%d_%H%M%S')}.dump"
    t = time.time()
    r = sh(f'pg_dump -h {H} -U {U} -Fc -f {f} {DB}')
    size = os.path.getsize(f) if os.path.exists(f) else 0
    ok = r.returncode == 0 and size > 1000
    if not ok and os.path.exists(f):
        os.remove(f)
    return {"status": "ok" if ok else "error", "file": os.path.basename(f),
            "size": size, "seconds": round(time.time() - t, 1), "error": r.stderr[-300:]}


def do_restore_test(name):
    if not name or "/" in name or ".." in name:
        return {"status": "error", "error": "nombre invalido"}
    f = f"{DIR}/{name}"
    if not os.path.exists(f):
        return {"status": "error", "error": "el archivo no existe"}
    t = time.time()
    sh(f'psql -h {H} -U {U} -d postgres -c "DROP DATABASE IF EXISTS restore_test"')
    sh(f'psql -h {H} -U {U} -d postgres -c "CREATE DATABASE restore_test"')
    r = sh(f'pg_restore -h {H} -U {U} -d restore_test --no-owner {f}')
    rows, expected = count("restore_test"), count(DB)
    sh(f'psql -h {H} -U {U} -d postgres -c "DROP DATABASE IF EXISTS restore_test"')
    ok = expected > 0 and rows == expected
    return {"status": "ok" if ok else "error", "rows": rows, "expected": expected,
            "seconds": round(time.time() - t, 1), "error": r.stderr[-300:]}


class Handler(BaseHTTPRequestHandler):
    def reply(self, code, body):
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(body).encode())

    def do_POST(self):
        auth = self.headers.get("Authorization", "")
        if not hmac.compare_digest(auth, f"Bearer {TOKEN}"):
            return self.reply(401, {"status": "error", "error": "no autorizado"})
        n = int(self.headers.get("Content-Length", 0))
        try:
            data = json.loads(self.rfile.read(n) or b"{}")
        except ValueError:
            return self.reply(400, {"status": "error", "error": "JSON invalido"})
        if self.path == "/backup":
            self.reply(200, do_backup())
        elif self.path == "/restore-test":
            self.reply(200, do_restore_test(data.get("file", "")))
        else:
            self.reply(404, {"status": "error", "error": "ruta no encontrada"})


HTTPServer(("0.0.0.0", 8080), Handler).serve_forever()