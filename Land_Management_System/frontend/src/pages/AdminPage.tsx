import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Stack,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import type { Unit, UserRole } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import { roleLabel } from "../utils/labels";

type UserRow = {
  id: number;
  username: string;
  email: string;
  phone_number?: string;
  role: UserRole;
  unit_id: number;
  is_active: boolean;
  must_change_password: boolean;
  last_login_at?: string;
};

export default function AdminPage() {
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const { me } = useAuth();
  const [q, setQ] = useState("");
  const [rows, setRows] = useState<UserRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [fullName, setFullName] = useState("");

  const [openCreate, setOpenCreate] = useState(false);
  const [openEdit, setOpenEdit] = useState<null | UserRow>(null);

  const [role, setRole] = useState<UserRole>("PROVINCE_OFFICER");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("123456");
  const [mustChange, setMustChange] = useState(true);

  const [provinces, setProvinces] = useState<Unit[]>([]);
  const [provinceId, setProvinceId] = useState<number | "">("");
  const [childUnits, setChildUnits] = useState<Unit[]>([]);
  const [childUnitId, setChildUnitId] = useState<number | "">("");
  const [provinceSearch, setProvinceSearch] = useState("");
  const [childSearch, setChildSearch] = useState("");

  const load = async () => {
    setLoading(true);

    try {
      const res = await api.get("/admin/users", {
        params: { q: q.trim() || undefined },
      });

      const data = Array.isArray(res.data)
        ? res.data
        : res.data?.data ?? [];

      setRows(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProvinces = async () => {
    const res = await api.get<Unit[]>("/units/provinces");
    setProvinces(res.data);
  };

  const loadChildren = async (pid: number) => {
    const res = await api.get<Unit[]>(`/units/${pid}/children`, { params: { q: childSearch.trim() || undefined } });
    setChildUnits(res.data);
  };

  const filteredProvinces = useMemo(() => {
    const q = provinceSearch.trim().toLowerCase();
    if (!q) return provinces;
    return provinces.filter((p) => p.name.toLowerCase().includes(q));
  }, [provinces, provinceSearch]);

  const unitIdToUse = useMemo(() => {
    if (role === "CENTRAL_OFFICER" || role === "ADMIN") return null; // admin will be created in central via API if needed
    if (role === "PROVINCE_OFFICER") return typeof provinceId === "number" ? provinceId : null;
    if (role === "COMMUNE_OFFICER") return typeof childUnitId === "number" ? childUnitId : null;
    return null;
  }, [role, provinceId, childUnitId]);

  return (
    <Stack spacing={2}>
      <Typography variant="h5" fontWeight={800}>
        Quản trị — Tài khoản
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} alignItems={{ sm: "center" }}>
          <TextField
            label="Tìm kiếm username"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            size="small"
            sx={{ minWidth: 260 }}
          />
          <Button variant="outlined" onClick={() => void load()}>
            Tìm
          </Button>
          <Box sx={{ flexGrow: 1 }} />
          <Button
            variant="contained"
            onClick={async () => {
              await loadProvinces();
              setRole("PROVINCE_OFFICER");
              setUsername("");
              setPassword("123456");
              setMustChange(true);
              setProvinceId("");
              setChildUnitId("");
              setChildUnits([]);
              setOpenCreate(true);
              setUsername("");
              setPassword("123456");
              setEmail("");
              setPhoneNumber("");
            }}
          >
            Tạo tài khoản
          </Button>
        </Stack>
      </Paper>

      <Paper sx={{ p: 2 }}>
        {loading ? (
          <Typography>Đang tải...</Typography>
        ) : rows.length === 0 ? (
          <Alert severity="info">Không có user.</Alert>
        ) : (
          <Stack spacing={1}>
            {(Array.isArray(rows) ? rows : []).filter((u) => u.role !== "ADMIN").map((u) => (
              <Paper key={u.id} variant="outlined" sx={{ p: 2 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography fontWeight={800}>{u.username}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      ID #{u.id} · {roleLabel(u.role)} · Đơn vị #{u.unit_id}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
    Đăng nhập cuối:
    {" "}
    {u.last_login_at
      ? new Date(u.last_login_at).toLocaleString("vi-VN")
      : "Chưa từng đăng nhập"}
  </Typography>

                  </Box>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Typography
                      variant="body2"
                      color={u.is_active ? "success.main" : "error.main"}
                    >
                      {u.is_active ? "Active" : "Inactive"}
                    </Typography>
                    <Switch
                      checked={u.is_active}
                      onChange={async (e) => {
                        const checked = e.target.checked;

                        setRows(prev =>
                          prev.map(item =>
                            item.id === u.id
                              ? { ...item, is_active: checked }
                              : item
                          )
                        );

                        try {
                          await api.put(`/admin/users/${u.id}`, {
                            is_active: checked,
                          });

                        } catch {
                          // rollback
                          setRows(prev =>
                            prev.map(item =>
                              item.id === u.id
                                ? { ...item, is_active: !checked }
                                : item
                            )
                          );
                          await load();
                        }
                      }}
                    />
                  </Stack>
                  <Button
                    variant="outlined"
                    onClick={async () => {
                      await loadProvinces();
                      setOpenEdit(u);
                      setRole(u.role);
                      setMustChange(u.must_change_password);
                      setProvinceId("");
                      setChildUnitId("");
                      setChildUnits([]);
                    }}
                  >
                    Sửa quyền
                  </Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        )}
      </Paper>

      <Dialog open={openCreate} onClose={() => setOpenCreate(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Tạo tài khoản</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Alert severity="info">
              Chọn cấp: <b>Trung ương / Tỉnh / Xã</b>. Nếu tạo user xã/phường: chọn <b>Tỉnh</b> rồi chọn <b>Xã/Phường</b>.
            </Alert>
            <TextField select label="Cấp" value={role} onChange={(e) => setRole(e.target.value as UserRole)} fullWidth>
              <MenuItem value="CENTRAL_OFFICER">Cán bộ Trung ương</MenuItem>
              <MenuItem value="PROVINCE_OFFICER">Cán bộ Tỉnh</MenuItem>
              <MenuItem value="COMMUNE_OFFICER">Cán bộ Xã/Phường</MenuItem>
            </TextField>
            {role === "PROVINCE_OFFICER" || role === "COMMUNE_OFFICER" ? (
              <>
                <TextField
                  size="small"
                  label="Tìm tỉnh"
                  value={provinceSearch}
                  onChange={(e) => setProvinceSearch(e.target.value)}
                  fullWidth
                />
                <TextField
                  select
                  label="Tỉnh"
                  value={provinceId}
                  onChange={async (e) => {
                    const v = Number(e.target.value);
                    setProvinceId(v);
                    setChildUnitId("");
                    await loadChildren(v);
                  }}
                  fullWidth
                >
                  {filteredProvinces.map((p) => (
                    <MenuItem key={p.id} value={p.id}>
                      {p.name}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            ) : null}
            {role === "COMMUNE_OFFICER" ? (
              <>
                <TextField
                  size="small"
                  label="Tìm xã/phường"
                  value={childSearch}
                  onChange={(e) => setChildSearch(e.target.value)}
                  fullWidth
                  disabled={!provinceId}
                />
                <TextField
                  select
                  label="Xã/Phường"
                  value={childUnitId}
                  onChange={(e) => setChildUnitId(Number(e.target.value))}
                  fullWidth
                  disabled={!provinceId}
                >
                  {childUnits.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      {c.name}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            ) : null}
            <TextField label="Username" value={username} onChange={(e) => setUsername(e.target.value)} fullWidth />
            <TextField label="Password" value={password} onChange={(e) => setPassword(e.target.value)} fullWidth />
              <TextField
  label="Họ và tên"
  value={fullName}
  onChange={(e) => setFullName(e.target.value)}
  fullWidth
/>
              <TextField
  label="Email"
  type="email"
  value={email}
  onChange={(e) => setEmail(e.target.value)}
  fullWidth
/>

<TextField
  label="Số điện thoại"
  value={phoneNumber}
  onChange={(e) => setPhoneNumber(e.target.value)}
  fullWidth
/>
            <Stack direction="row" spacing={1} alignItems="center">
              <Switch checked={mustChange} onChange={(e) => setMustChange(e.target.checked)} />
              <Typography>Yêu cầu đổi mật khẩu khi đăng nhập</Typography>
            </Stack>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCreate(false)}>Hủy</Button>
          <Button
            variant="contained"
            disabled={
  !username.trim() ||
  !email.trim() ||
  !password ||
  (role !== "CENTRAL_OFFICER" && unitIdToUse == null)
}
            onClick={async () => {
              try {
                const unit_id =
                  role === "CENTRAL_OFFICER" ? (me?.unit_id ?? 1) : (unitIdToUse as number);
                await api.post("/admin/users", {
  username,
  password,
  full_name: fullName,
  email,
  phone_number: phoneNumber,
  role,
  unit_id: unit_id,
  must_change_password: mustChange,
});
                setOpenCreate(false);
                await load();
              } catch (e: any) {
                alert(e?.response?.data?.detail ?? e?.message ?? "Tạo tài khoản thất bại");
              }
            }}
          >
            Tạo
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(openEdit)} onClose={() => setOpenEdit(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Sửa quyền / đổi đơn vị</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {openEdit ? (
              <Alert severity="info">
                User: <b>{openEdit.username}</b>
              </Alert>
            ) : null}
            <TextField select label="Cấp (role)" value={role} onChange={(e) => setRole(e.target.value as UserRole)} fullWidth>
              <MenuItem value="CENTRAL_OFFICER">Cán bộ Trung ương</MenuItem>
              <MenuItem value="PROVINCE_OFFICER">Cán bộ Tỉnh</MenuItem>
              <MenuItem value="COMMUNE_OFFICER">Cán bộ Xã/Phường</MenuItem>
            </TextField>
            {role === "PROVINCE_OFFICER" || role === "COMMUNE_OFFICER" ? (
              <>
                <TextField
                  size="small"
                  label="Tìm tỉnh"
                  value={provinceSearch}
                  onChange={(e) => setProvinceSearch(e.target.value)}
                  fullWidth
                />
                <TextField
                  select
                  label="Tỉnh"
                  value={provinceId}
                  onChange={async (e) => {
                    const v = Number(e.target.value);
                    setProvinceId(v);
                    setChildUnitId("");
                    await loadChildren(v);
                  }}
                  fullWidth
                >
                  {filteredProvinces.map((p) => (
                    <MenuItem key={p.id} value={p.id}>
                      {p.name}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            ) : null}
            {role === "COMMUNE_OFFICER" ? (
              <>
                <TextField
                  size="small"
                  label="Tìm xã/phường"
                  value={childSearch}
                  onChange={(e) => setChildSearch(e.target.value)}
                  fullWidth
                  disabled={!provinceId}
                />
                <TextField
                  select
                  label="Xã/Phường"
                  value={childUnitId}
                  onChange={(e) => setChildUnitId(Number(e.target.value))}
                  fullWidth
                  disabled={!provinceId}
                >
                  {childUnits.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      {c.name}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            ) : null}
            <Stack direction="row" spacing={1} alignItems="center">
              <Switch checked={mustChange} onChange={(e) => setMustChange(e.target.checked)} />
              <Typography>Yêu cầu đổi mật khẩu khi đăng nhập</Typography>
            </Stack>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenEdit(null)}>Đóng</Button>
          <Button
            variant="contained"
            disabled={!openEdit}
            onClick={async () => {
              if (!openEdit) return;
              try {
                const unit_id =
                  role === "CENTRAL_OFFICER" ? (me?.unit_id ?? 1) : (unitIdToUse as number | null);
                await api.put(`/admin/users/${openEdit.id}`, {
                  role,
                  unit_id,
                  must_change_password: mustChange,
                });
                setOpenEdit(null);
                await load();
              } catch (e: any) {
                alert(e?.response?.data?.detail ?? e?.message ?? "Cập nhật thất bại");
              }
            }}
          >
            Lưu
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}


