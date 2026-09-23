import { useState } from "react";
import { checkAccess } from "../api/client";
import "./GateScreen.css";

const STATUS_LABELS = {
  GRANTED: { text: "PRISTUP ODOBREN", className: "status-granted" },
  DENIED: { text: "PRISTUP ODBIJEN", className: "status-denied" },
  LOW_CONFIDENCE: { text: "NEIZVESNO - RUČNA PROVERA", className: "status-warning" },
};

function GateScreen() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [direction, setDirection] = useState("ENTRY");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
    setResult(null);
    setError(null);
  };

  const handleCheck = async () => {
    if (!selectedFile) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await checkAccess(selectedFile, direction);
      setResult(response.data);
    } catch (err) {
      // Backend je i dalje dostupan ali je nesto poslo naopako (npr. ML
      // servis ne radi) - razlikujemo to od "nema odgovora uopste".
      setError(
        err.response
          ? "Greška pri proveri pristupa. Pokušaj ponovo."
          : "Backend nije dostupan. Proveri da li je pokrenut na portu 8080."
      );
    } finally {
      setLoading(false);
    }
  };

  const statusInfo = result ? STATUS_LABELS[result.status] : null;

  return (
    <div className="gate-container">
      <h1>Kontrola pristupa - simulacija kapije</h1>
      <p className="subtitle">
        Uploaduj fotografiju vozila da simuliraš detekciju kamere na kapiji.
      </p>

      <div className="direction-toggle">
        <label>
          <input
            type="radio"
            name="direction"
            value="ENTRY"
            checked={direction === "ENTRY"}
            onChange={() => setDirection("ENTRY")}
          />
          Ulazak
        </label>
        <label>
          <input
            type="radio"
            name="direction"
            value="EXIT"
            checked={direction === "EXIT"}
            onChange={() => setDirection("EXIT")}
          />
          Izlazak
        </label>
      </div>

      <input type="file" accept="image/*" onChange={handleFileChange} />

      {previewUrl && (
        <div className="preview-box">
          <img src={previewUrl} alt="Pregled uploadovane slike" />
        </div>
      )}

      <button onClick={handleCheck} disabled={!selectedFile || loading}>
        {loading ? "Proveravam..." : "Proveri pristup"}
      </button>

      {error && <div className="error-box">{error}</div>}

      {result && (
        <div className={`result-box ${statusInfo.className}`}>
          <div className="result-status">{statusInfo.text}</div>
          {result.recognizedPlate && (
            <div className="result-plate">
              Prepoznata tablica: <strong>{result.recognizedPlate}</strong>
            </div>
          )}
          {result.detectionConfidence != null && (
            <div className="result-confidence">
              Pouzdanost detekcije: {(result.detectionConfidence * 100).toFixed(1)}%
            </div>
          )}
          <div className="result-message">{result.message}</div>
        </div>
      )}
    </div>
  );
}

export default GateScreen;
