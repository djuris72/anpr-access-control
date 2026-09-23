import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getVehicles, createVehicle, deleteVehicle } from "../api/client";
import "./AdminDashboard.css";

const EMPTY_FORM = {
  plateNumber: "",
  ownerName: "",
  accessLevel: "RESIDENT",
};

function AdminDashboard() {
  const [vehicles, setVehicles] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadVehicles = async () => {
    setLoading(true);
    try {
      const response = await getVehicles();
      setVehicles(response.data);
      setError(null);
    } catch (err) {
      setError("Ne mogu da učitam listu vozila.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVehicles();
  }, []);

  const handleAdd = async (event) => {
    event.preventDefault();
    try {
      await createVehicle({ ...form, active: true });
      setForm(EMPTY_FORM);
      loadVehicles();
    } catch (err) {
      setError("Ne mogu da dodam vozilo. Proveri da li tablica već postoji.");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Obrisati ovo vozilo sa liste?")) return;
    try {
      await deleteVehicle(id);
      loadVehicles();
    } catch (err) {
      setError("Ne mogu da obrišem vozilo.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/admin/login");
  };

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h1>Admin panel - Whitelist vozila</h1>
        <button className="logout-button" onClick={handleLogout}>
          Odjava
        </button>
      </div>

      <form className="add-vehicle-form" onSubmit={handleAdd}>
        <input
          type="text"
          placeholder="Tablica (npr. NS123AB)"
          value={form.plateNumber}
          onChange={(e) => setForm({ ...form, plateNumber: e.target.value })}
          required
        />
        <input
          type="text"
          placeholder="Ime vlasnika"
          value={form.ownerName}
          onChange={(e) => setForm({ ...form, ownerName: e.target.value })}
          required
        />
        <select
          value={form.accessLevel}
          onChange={(e) => setForm({ ...form, accessLevel: e.target.value })}
        >
          <option value="RESIDENT">Stanar</option>
          <option value="STAFF">Osoblje</option>
          <option value="GUEST">Gost</option>
        </select>
        <button type="submit">Dodaj vozilo</button>
      </form>

      {error && <div className="admin-error">{error}</div>}

      {loading ? (
        <p>Učitavanje...</p>
      ) : (
        <table className="vehicles-table">
          <thead>
            <tr>
              <th>Tablica</th>
              <th>Vlasnik</th>
              <th>Nivo pristupa</th>
              <th>Aktivno</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {vehicles.length === 0 && (
              <tr>
                <td colSpan="5" className="empty-row">
                  Nema vozila na listi.
                </td>
              </tr>
            )}
            {vehicles.map((vehicle) => (
              <tr key={vehicle.id}>
                <td>{vehicle.plateNumber}</td>
                <td>{vehicle.ownerName}</td>
                <td>{vehicle.accessLevel}</td>
                <td>{vehicle.active ? "Da" : "Ne"}</td>
                <td>
                  <button
                    className="delete-button"
                    onClick={() => handleDelete(vehicle.id)}
                  >
                    Obriši
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default AdminDashboard;
