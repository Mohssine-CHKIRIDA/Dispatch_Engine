import logging
import threading

from fastapi import FastAPI
import uvicorn

from consumer import EvaluationConsumer
import db
import kpi

logging.basicConfig(level=logging.INFO)

app = FastAPI()
_consumer = EvaluationConsumer()


@app.on_event("startup")
def start_consumer_thread():
    thread = threading.Thread(target=_consumer.run, daemon=True)
    thread.start()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/kpis")
def get_kpis():
    conn = db.get_conn()
    try:
        return kpi.compute_kpis(conn)
    finally:
        conn.close()


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8091)