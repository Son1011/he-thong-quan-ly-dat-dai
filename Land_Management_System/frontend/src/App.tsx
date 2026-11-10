import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { CircularProgress, Box } from "@mui/material";
import LoginPage from "./pages/LoginPage";
import DossierListPage from "./pages/DossierListPage";
import InboxPage from "./pages/InboxPage";
import NewDossierPage from "./pages/NewDossierPage";
import DossierDetailPage from "./pages/DossierDetailPage";
import AdminPage from "./pages/AdminPage";
import ChangePasswordPage from "./pages/ChangePasswordPage";
import UnitsBrowserPage from "./pages/UnitsBrowserPage";
import SentToCentralPage from "./pages/SentToCentralPage";
import StatisticsPage from "./pages/StatisticsPage";
import AppShell from "./layout/AppShell";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { hasToken } from "./auth/auth";

function PrivateRoutes() {
  const { me, loading } = useAuth();
  const loc = useLocation();

  const passwordExpired = !!(
    me?.password_expires_at &&
    new Date(me.password_expires_at).getTime() < Date.now()
  );

  if (loading) {
    return (
      <Box sx={{ height: "100vh", display: "grid", placeItems: "center" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!me || !hasToken()) {
    return <Navigate to="/login" replace />;
  }

  if (
    (me.must_change_password || passwordExpired) &&
    loc.pathname !== "/change-password"
  ) {
    return <Navigate to="/change-password" replace />;
  }

  // ⭐ REDIRECT THEO ROLE Ở ĐÂY
  if (loc.pathname === "/") {
    if (me.role === "ADMIN") {
      return <Navigate to="/dossiers" replace />;
    }
    return <Navigate to="/inbox" replace />;
  }

  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Navigate to="/inbox" replace />} />
        <Route path="/inbox" element={<InboxPage />} />
        <Route path="/change-password" element={<ChangePasswordPage />} />
        <Route path="/units" element={<UnitsBrowserPage />} />
        <Route path="/sent-to-central" element={<SentToCentralPage />} />
        <Route path="/stats" element={<StatisticsPage />} />
        <Route path="/dossiers" element={<DossierListPage />} />
        <Route path="/dossiers/new" element={<NewDossierPage />} />
        <Route path="/dossiers/:id" element={<DossierDetailPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="*" element={<Navigate to="/inbox" replace />} />
      </Routes>
    </AppShell>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/*" element={<PrivateRoutes />} />
      </Routes>
    </AuthProvider>
  );
}


