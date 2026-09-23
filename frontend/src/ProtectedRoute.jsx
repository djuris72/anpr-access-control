import { Navigate } from "react-router-dom";

/**
 * Ne validira token na frontendu (to i dalje radi backend na svakom
 * zahtevu) - samo sprecava da neko slucajno vidi admin panel bez ikakvog
 * tokena. Prava provera je uvek na backend-u.
 */
function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");
  if (!token) {
    return <Navigate to="/admin/login" replace />;
  }
  return children;
}

export default ProtectedRoute;
