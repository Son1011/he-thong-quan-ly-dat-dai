import React from "react";
import {
  AppBar,
  Box,
  Button,
  Container,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import InboxIcon from "@mui/icons-material/Inbox";
import ListAltIcon from "@mui/icons-material/ListAlt";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import AccountTreeIcon from "@mui/icons-material/AccountTree";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import BarChartIcon from "@mui/icons-material/BarChart";
import LockResetIcon from "@mui/icons-material/LockReset";
import LogoutIcon from "@mui/icons-material/Logout";
import { Link as RouterLink, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { roleLabel } from "../utils/labels";
import Footer from "../components/Footer";
import { publicImageUrl } from "../utils/publicAssets";

const logoUrl = publicImageUrl("đất đai.png");

type NavItem = {
  label: string;
  to: string;
  icon: React.ReactNode;
  roles?: string[];
};

const drawerWidth = 270;

export default function AppShell({ children }: { children: React.ReactNode }) {
  const { me, signOut } = useAuth();
  const loc = useLocation();
  const isMobile = useMediaQuery((theme: any) => theme.breakpoints.down("md"));
  const [mobileOpen, setMobileOpen] = React.useState(false);

  const items: NavItem[] = [
    { label: "Hồ sơ cần xử lý", to: "/inbox", icon: <InboxIcon />,roles: ["CENTRAL_OFFICER", "PROVINCE_OFFICER", "COMMUNE_OFFICER"] },
    { label: "Danh sách hồ sơ", to: "/dossiers", icon: <ListAltIcon /> },
    { label: "Tra cứu theo tỉnh / xã", to: "/units", icon: <AccountTreeIcon />, roles: ["CENTRAL_OFFICER", "PROVINCE_OFFICER"] },
    { label: "Hồ sơ gửi lên Trung ương", to: "/sent-to-central", icon: <CloudUploadIcon />, roles: ["PROVINCE_OFFICER"] },
    { label: "Thống kê", to: "/stats", icon: <BarChartIcon />, roles: ["CENTRAL_OFFICER", "PROVINCE_OFFICER"] },
    { label: "Tạo hồ sơ", to: "/dossiers/new", icon: <AddCircleOutlineIcon />, roles: ["COMMUNE_OFFICER", "PROVINCE_OFFICER"] },
    { label: "Đổi mật khẩu", to: "/change-password", icon: <LockResetIcon /> },
    { label: "Quản trị hệ thống", to: "/admin", icon: <AdminPanelSettingsIcon />, roles: ["ADMIN"] },
  ];

  const visible = items.filter((it) => !it.roles || (me && it.roles.includes(me.role)));

  const drawerContent = (
    <Box sx={{ overflow: "auto" }}>
      <Box sx={{ px: 2, py: 2 }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1.25 }}>
          <img src={logoUrl} width={40} height={40} alt="Logo" style={{ objectFit: "contain" }} />
          <Box>
            <Typography fontWeight={900} lineHeight={1.1}>
              Quản lý hồ sơ đất đai
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Theo mô hình hành chính 3 cấp
            </Typography>
          </Box>
        </Box>
      </Box>
      <Divider />
      <List>
        {visible.map((it) => (
          <ListItemButton
            key={it.to}
            component={RouterLink}
            to={it.to}
            selected={loc.pathname === it.to}
            onClick={() => setMobileOpen(false)}
          >
            <ListItemIcon>{it.icon}</ListItemIcon>
            <ListItemText primary={it.label} />
          </ListItemButton>
        ))}
      </List>
      <Divider />
    </Box>
  );

  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <Box sx={{ display: "flex", flexGrow: 1 }}>
        <AppBar position="fixed" elevation={1} sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
          <Toolbar>
            {isMobile ? (
              <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
                <MenuIcon />
              </IconButton>
            ) : null}
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mr: 2 }}>
              <img src={logoUrl} width={32} height={32} alt="Logo" style={{ objectFit: "contain" }} />
            </Box>
            <Typography variant="h6" fontWeight={700} sx={{ flexGrow: 1 }}>
              Hệ thống quản lý hồ sơ đất đai
            </Typography>
            <Typography variant="body2" sx={{ mr: 2, color: "rgba(255,255,255,0.9)" }}>
              {me ? `${me.username} · ${roleLabel(me.role)} · đơn vị #${me.unit_id}` : ""}
            </Typography>
            <Button color="inherit" startIcon={<LogoutIcon />} onClick={signOut}>
              Đăng xuất
            </Button>
          </Toolbar>
        </AppBar>

        <Drawer
          variant={isMobile ? "temporary" : "permanent"}
          open={isMobile ? mobileOpen : true}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            width: drawerWidth,
            flexShrink: 0,
            [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: "border-box" },
          }}
        >
          <Toolbar />
          {drawerContent}
        </Drawer>

        <Box component="main" sx={{ flexGrow: 1, p: 3, display: "flex", flexDirection: "column" }}>
          <Toolbar />
          <Container maxWidth="lg" sx={{ flexGrow: 1 }}>{children}</Container>
          <Footer />
        </Box>
      </Box>
    </Box>
  );
}


