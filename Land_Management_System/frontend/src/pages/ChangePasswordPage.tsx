import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

export default function ChangePasswordPage() {
  const nav = useNavigate();
  const { me, refresh } = useAuth();

  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const passwordExpired =
    !!me?.password_expires_at &&
    new Date(me.password_expires_at).getTime() < Date.now();

  const forced =
    Boolean(me?.must_change_password) || passwordExpired;

  const handleSubmit = async () => {
    setErr(null);

    if (!oldPassword.trim()) {
      setErr("Vui lòng nhập mật khẩu cũ");
      return;
    }

    if (newPassword.length < 6) {
      setErr("Mật khẩu mới phải có ít nhất 6 ký tự");
      return;
    }

    if (newPassword !== confirm) {
      setErr("Mật khẩu mới không khớp");
      return;
    }

    setBusy(true);

    try {
      await api.post("/auth/change-password", {
        old_password: oldPassword,
        new_password: newPassword,
        confirm_password: confirm,
      });

      await refresh();

      nav("/inbox", { replace: true });
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ||
        e?.response?.data?.detail ||
        e?.message ||
        "";

      if (
        msg.toLowerCase().includes("old password") ||
        msg.toLowerCase().includes("current password") ||
        msg.toLowerCase().includes("incorrect") ||
        msg.toLowerCase().includes("invalid password")
      ) {
        setErr("Mật khẩu cũ sai");
      } else {
        setErr(msg || "Đổi mật khẩu thất bại");
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h5" fontWeight={800}>
          Đổi mật khẩu
        </Typography>

        <Typography variant="body2" color="text.secondary">
          {forced
            ? "Tài khoản này cần đổi mật khẩu trước khi tiếp tục."
            : "Bạn có thể đổi mật khẩu bất cứ lúc nào."}
        </Typography>

        {passwordExpired && (
          <Typography
            color="error"
            fontWeight={700}
            sx={{ mt: 1 }}
          >
            Mật khẩu hết hạn
          </Typography>
        )}
      </Box>

      <Paper sx={{ p: 2 }}>
        <Stack spacing={2}>
          {err && (
            <Alert severity="error">
              {err}
            </Alert>
          )}

          <TextField
            label="Mật khẩu cũ"
            type="password"
            value={oldPassword}
            onChange={(e) => setOldPassword(e.target.value)}
            fullWidth
          />

          <TextField
            label="Mật khẩu mới"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            helperText="Tối thiểu 6 ký tự"
            fullWidth
          />

          <TextField
            label="Xác nhận mật khẩu mới"
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            fullWidth
          />

          <Stack direction="row" spacing={1}>
            <Button
              variant="contained"
              disabled={busy}
              onClick={handleSubmit}
            >
              {busy ? "Đang lưu..." : "Lưu"}
            </Button>

            <Button
              variant="outlined"
              disabled={busy || forced}
              onClick={() => nav(-1)}
            >
              Hủy
            </Button>
          </Stack>
        </Stack>
      </Paper>
    </Stack>
  );
}