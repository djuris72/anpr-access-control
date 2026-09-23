"""
ANPR ML servis
==============
FastAPI wrapper oko YOLOv8 + EasyOCR pipeline-a za prepoznavanje registarskih
tablica (preuzeto i prilagođeno iz istraživačkog notebook-a — najbolji od
tri testirana pristupa: OpenCV, Custom CNN, YOLOv8+EasyOCR).

Endpoint:
    POST /recognize   -> upload slike, vraća listu detektovanih tablica
    GET  /health       -> provera da li je servis živ i da li je model učitan
"""

import os
import re
import logging

import cv2
import numpy as np
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from ultralytics import YOLO
import easyocr

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("anpr-ml-service")

# ---------------------------------------------------------------------------
# Konfiguracija
# ---------------------------------------------------------------------------
WEIGHTS_PATH = os.environ.get("WEIGHTS_PATH", "weights/best.pt")
CONF_THRESHOLD = float(os.environ.get("CONF_THRESHOLD", "0.25"))
OCR_LANGUAGES = ["en"]

app = FastAPI(title="ANPR ML Service", version="1.0.0")

# Model i OCR reader se učitavaju JEDNOM pri pokretanju servisa (ne po requestu,
# jer je učitavanje YOLO/EasyOCR modela sporo).
yolo_model: YOLO | None = None
ocr_reader: easyocr.Reader | None = None


@app.on_event("startup")
def load_models():
    global yolo_model, ocr_reader

    if not os.path.exists(WEIGHTS_PATH):
        logger.warning(
            f"Fajl sa težinama nije pronađen na '{WEIGHTS_PATH}'. "
            "Servis će se pokrenuti, ali /recognize će vraćati grešku "
            "dok ne dodaš pravi best.pt."
        )
        yolo_model = None
    else:
        logger.info(f"Učitavam YOLO model iz {WEIGHTS_PATH} ...")
        yolo_model = YOLO(WEIGHTS_PATH)
        logger.info("YOLO model učitan.")

    logger.info("Učitavam EasyOCR reader ...")
    ocr_reader = easyocr.Reader(OCR_LANGUAGES, gpu=False)
    logger.info("EasyOCR reader učitan.")


# ---------------------------------------------------------------------------
# Pipeline funkcije — 1:1 preuzeto iz notebook-a (detect_and_read_plate)
# ---------------------------------------------------------------------------
def preprocess_plate_region(crop: np.ndarray) -> np.ndarray:
    """
    Predobrada izdvojenog regiona registarske tablice pre OCR-a:
    grayscale -> upscale 2x -> adaptivna binarizacija.
    """
    gray = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
    scale = 2
    gray = cv2.resize(gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
    thresh = cv2.adaptiveThreshold(
        gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY, 31, 15
    )
    return thresh


def clean_plate_text(raw_text: str) -> str:
    """Uklanja sve osim velikih slova i brojeva."""
    text = raw_text.upper()
    text = re.sub(r"[^A-Z0-9]", "", text)
    return text


def detect_and_read_plate(image: np.ndarray, conf: float = CONF_THRESHOLD) -> list[dict]:
    """
    Vraća listu detekcija: [{box, confidence, text, ocr_confidence}, ...]
    Radi nad već učitanom slikom (numpy array), ne nad putanjom na disku,
    jer u API-ju slika stiže kao upload, ne kao fajl na disku.
    """
    if yolo_model is None:
        raise RuntimeError(
            "YOLO model nije učitan — proveri da li best.pt postoji na WEIGHTS_PATH."
        )

    results = yolo_model.predict(image, conf=conf, verbose=False)
    detections = []

    boxes = results[0].boxes
    h, w = image.shape[:2]

    for box in boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
        det_conf = float(box.conf[0].cpu().numpy())

        pad = 4
        crop = image[max(0, y1 - pad):min(h, y2 + pad),
                      max(0, x1 - pad):min(w, x2 + pad)]

        if crop.size == 0:
            continue

        processed = preprocess_plate_region(crop)
        ocr_results = ocr_reader.readtext(processed)

        if ocr_results:
            best = max(ocr_results, key=lambda r: r[2])
            text = clean_plate_text(best[1])
            ocr_conf = float(best[2])
        else:
            text = ""
            ocr_conf = 0.0

        detections.append({
            "box": [x1, y1, x2, y2],
            "confidence": round(det_conf, 4),
            "text": text,
            "ocr_confidence": round(ocr_conf, 4),
        })

    return detections


# ---------------------------------------------------------------------------
# API endpoint-i
# ---------------------------------------------------------------------------
@app.get("/health")
def health():
    return {
        "status": "ok",
        "model_loaded": yolo_model is not None,
        "weights_path": WEIGHTS_PATH,
    }


@app.post("/recognize")
async def recognize(file: UploadFile = File(...)):
    if yolo_model is None:
        raise HTTPException(
            status_code=503,
            detail="Model nije učitan na serveru (nedostaje best.pt). "
                   "Postavi WEIGHTS_PATH ili dodaj fajl u weights/best.pt.",
        )

    contents = await file.read()
    npimg = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(npimg, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="Fajl nije validna slika.")

    try:
        detections = detect_and_read_plate(image)
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))

    if not detections:
        return JSONResponse({"detected": False, "plates": []})

    # Vrati sve detekcije, sortirane po detection confidence-u (najbolja prva)
    detections.sort(key=lambda d: d["confidence"], reverse=True)

    return {
        "detected": True,
        "plates": detections,
        "best_plate": detections[0]["text"],
        "best_confidence": detections[0]["confidence"],
    }
