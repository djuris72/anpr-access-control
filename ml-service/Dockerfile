# Bazna slika sa Python-om. "slim" varijanta je manja od pune, ali ima
# dovoljno da instaliramo sve zavisnosti (za razliku od "alpine", koji zna
# da pravi probleme sa bibliotekama kao sto su opencv/torch).
FROM python:3.11-slim

# Sistemske biblioteke koje OpenCV zahteva da bi uopste radio unutar
# kontejnera (bez ovoga, "import cv2" puca sa "libGL.so.1 not found").
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Prvo kopiramo SAMO requirements.txt i instaliramo zavisnosti - Docker
# kesira ovaj korak, pa se sledeci build (posle izmene main.py) ne mora
# iznova povlaciti torch/ultralytics/easyocr (nekoliko stotina MB), vec
# samo ako se requirements.txt promeni.
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Tek sada kopiramo ostatak koda (i weights/best.pt).
COPY . .

EXPOSE 8000

# --host 0.0.0.0 je OBAVEZNO ovde (za razliku od lokalnog pokretanja gde
# je opciono) - bez toga servis slusa samo unutar kontejnera i spolja mu
# se ne moze pristupiti ni preko mapiranog porta.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
