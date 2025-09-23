from fastapi import FastAPI, Body
from pydantic import BaseModel, HttpUrl
import os
import time
import requests

app = FastAPI(title="YOLO Stub API", version="0.1.0")

# CORS를 열 필요는 없음(내부 호출이라면). 꼭 필요하면 아래 주석 해제:
# from fastapi.middleware.cors import CORSMiddleware
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],  # 내부만 쓸거면 정확히 지정 권장
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

class InferRequest(BaseModel):
    s3_url: HttpUrl

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/hello")
def hello():
    return {"message": "hello from fastapi"}

@app.post("/infer")
def infer(req: InferRequest):
    """
    실제 YOLO 붙이기 전까지는 더미 로직.
    - s3_url로부터 파일 HEAD만 때려서 접근 가능 여부 체크
    - 임시로 bounding boxes 가짜 데이터 반환
    """
    try:
        # S3 접근 체크 (퍼블릭 or 사전서명 URL이면 200/403 등 응답 올 것)
        head = requests.head(req.s3_url, timeout=5)
        reachable = head.status_code < 400
    except Exception:
        reachable = False

    # 더미 결과
    fake_boxes = [
        {"label": "shirt", "confidence": 0.88, "bbox": [10, 20, 120, 180]},
        {"label": "pants", "confidence": 0.76, "bbox": [130, 200, 260, 420]},
    ]
    return {
        "ok": True,
        "image_reachable": reachable,
        "model": os.getenv("MODEL_NAME", "yolov8n-stub"),
        "latency_ms": int(100 + (time.time() * 10) % 50),  # 장난감 수치
        "boxes": fake_boxes,
    }
