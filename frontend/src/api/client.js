import axios from "axios";

// Backend (Spring Boot) adresa - u produkciji bi ovo islo kao environment
// varijabla (npr. VITE_API_URL), ali za lokalni razvoj je dovoljno ovako.
const API_BASE_URL = "http://localhost:8080";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Automatski dodaje JWT token na svaki zahtev ako postoji u localStorage -
// tako ne moramo rucno da ga prosledjujemo iz svake komponente.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Ako backend vrati 401 (token istekao/nevalidan), izbacujemo korisnika
// nazad na login umesto da admin panel ostane "zaglavljen" u praznom stanju.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/admin/login";
    }
    return Promise.reject(error);
  }
);

export const login = (username, password) =>
  apiClient.post("/api/auth/login", { username, password });

export const getVehicles = () => apiClient.get("/api/vehicles");

export const createVehicle = (vehicle) =>
  apiClient.post("/api/vehicles", vehicle);

export const updateVehicle = (id, vehicle) =>
  apiClient.put(`/api/vehicles/${id}`, vehicle);

export const deleteVehicle = (id) => apiClient.delete(`/api/vehicles/${id}`);

export const checkAccess = (imageFile, direction) => {
  const formData = new FormData();
  formData.append("file", imageFile);
  return apiClient.post(
    `/api/access/check?direction=${direction}`,
    formData,
    { headers: { "Content-Type": "multipart/form-data" } }
  );
};

export default apiClient;
