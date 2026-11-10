import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import type { Dossier } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import SignaturePad from "../components/SignaturePad";
import { StatusChip } from "../components/StatusChip";

type Action = "approve" | "return" | "escalate" | "submit";

type HistoryRow = {
  id: number;
  dossier_id: number;
  action: string;
  note?: string | null;
  signature_base64_png?: string | null;
  actor_user_id: number;
  actor_unit_id: number;
  actor_username?: string | null;
  actor_unit_name?: string | null;
  from_unit_id?: number | null;
  to_unit_id?: number | null;
  created_at: string;
};

export default function DossierDetailPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const { me } = useAuth();
  const dossierId = Number(id);
  const [row, setRow] = useState<Dossier | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [open, setOpen] = useState(false);
  const [action, setAction] = useState<Action>("approve");
  const [note, setNote] = useState("");
  const [signature, setSignature] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [attachments, setAttachments] = useState<
    Array<{ id: number; original_filename: string; content_type: string; uploaded_at: string }>
  >([]);
  const [uploading, setUploading] = useState(false);
  const [history, setHistory] = useState<HistoryRow[]>([]);

  const canActions: Action[] = useMemo(() => {
    if (!me || !row) return [];
    const assigned = row.assigned_to_unit_id === me.unit_id;
    if (!assigned) return [];
    if (me.role === "PROVINCE_OFFICER") return ["approve", "return", "escalate"];
    if (me.role === "CENTRAL_OFFICER") return ["approve", "return"];
    if (me.role === "COMMUNE_OFFICER") return ["submit"];
    return [];
  }, [me, row]);

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setErr(null);
      try {
        const res = await api.get<Dossier>(`/dossiers/${dossierId}`);
        setRow(res.data);
        const at = await api.get(`/dossiers/${dossierId}/attachments`);
        setAttachments(at.data ?? []);
        const hi = await api.get<HistoryRow[]>(`/dossiers/${dossierId}/history`);
        setHistory(hi.data ?? []);
      } catch (e: any) {
        setErr(e?.response?.data?.detail ?? e?.message ?? "Không tải được hồ sơ");
      } finally {
        setLoading(false);
      }
    };
    if (Number.isFinite(dossierId)) void run();
  }, [dossierId]);

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h5" fontWeight={800}>
          Chi tiết hồ sơ
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Thao tác duyệt chỉ xuất hiện khi hồ sơ đang “assigned” về đơn vị của bạn.
        </Typography>
      </Box>

      {err ? <Alert severity="error">{err}</Alert> : null}

      <Paper sx={{ p: 2 }}>
        {loading || !row ? (
          <Typography>Đang tải...</Typography>
        ) : (
          <Stack spacing={1}>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
              <Box sx={{ flexGrow: 1 }}>
                <Typography fontWeight={800}>{row.title}</Typography>
                <Typography variant="body2" color="text.secondary">
                  ID #{row.id} · origin unit #{row.origin_unit_id} · assigned unit #{row.assigned_to_unit_id}
                </Typography>
              </Box>
              <StatusChip status={row.status} />
            </Stack>
            <Stack direction="row" spacing={1} sx={{ mt: 1 }} flexWrap="wrap" useFlexGap>
              <Button variant="outlined" onClick={() => nav(-1)}>
                Quay lại
              </Button>
              <Button
                variant="outlined"
                component="label"
                disabled={uploading}
              >
                Tải tài liệu lên
                <input
                  hidden
                  type="file"
                  onChange={async (e) => {
                    const f = e.target.files?.[0];
                    if (!f || !row) return;
                    setUploading(true);
                    try {
                      const fd = new FormData();
                      fd.append("file", f);
                      await api.post(`/dossiers/${row.id}/attachments`, fd, {
                        headers: { "Content-Type": "multipart/form-data" },
                      });
                      const at = await api.get(`/dossiers/${row.id}/attachments`);
                      setAttachments(at.data ?? []);
                    } catch (err: any) {
                      alert(err?.response?.data?.detail ?? err?.message ?? "Upload thất bại");
                    } finally {
                      setUploading(false);
                      e.target.value = "";
                    }
                  }}
                />
              </Button>
              {canActions.map((a) => (
                <Button
                  key={a}
                  variant={a === "return" ? "outlined" : "contained"}
                  color={a === "return" ? "inherit" : a === "approve" ? "success" : a === "escalate" ? "warning" : "primary"}
                  onClick={() => {
                    setAction(a);
                    setNote("");
                    setSignature(null);
                    setOpen(true);
                  }}
                >
                  {a === "approve" ? "Duyệt" : a === "return" ? "Trả lại" : a === "escalate" ? "Gửi cấp trên" : "Gửi lại"}
                </Button>
              ))}
            </Stack>
          </Stack>
        )}
      </Paper>

      <Paper sx={{ p: 2 }}>
        <Typography fontWeight={800} sx={{ mb: 1 }}>
          Tài liệu đính kèm
        </Typography>
        {attachments.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            Chưa có tài liệu.
          </Typography>
        ) : (
          <Stack spacing={1}>
            {attachments.map((a) => (
              <Paper key={a.id} variant="outlined" sx={{ p: 2 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography fontWeight={700}>{a.original_filename}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {a.content_type}
                    </Typography>
                  </Box>
                  <Button
                    variant="contained"
                    onClick={async () => {
                      try {
                        const res = await api.get(`/attachments/${a.id}/download`, { responseType: "blob" });
                        const blob = res.data as Blob;
                        const url = URL.createObjectURL(blob);
                        window.open(url, "_blank");
                        setTimeout(() => URL.revokeObjectURL(url), 30_000);
                      } catch (e: any) {
                        alert(e?.response?.data?.detail ?? e?.message ?? "Không mở được tài liệu");
                      }
                    }}
                  >
                    Mở
                  </Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        )}
      </Paper>

      <Paper sx={{ p: 2 }}>
        <Typography fontWeight={800} sx={{ mb: 1 }}>
          Lịch sử xử lý
        </Typography>
        {loading ? (
          <Typography>Đang tải...</Typography>
        ) : history.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            Chưa có lịch sử.
          </Typography>
        ) : (
          <Stack spacing={1}>
            {history.map((h) => (
              <Paper key={h.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1}>
                  <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography fontWeight={800}>
                        {h.action.toUpperCase()}{" "}
                        <Typography component="span" variant="body2" color="text.secondary">
                          · {new Date(h.created_at).toLocaleString()}
                        </Typography>
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {h.actor_username ?? `user#${h.actor_user_id}`} · {h.actor_unit_name ?? `unit#${h.actor_unit_id}`}
                      </Typography>
                      {h.note ? (
                        <Typography variant="body2" sx={{ mt: 0.5 }}>
                          Ghi chú: {h.note}
                        </Typography>
                      ) : null}
                    </Box>
                  </Stack>
                  {h.signature_base64_png ? (
                    <Box sx={{ mt: 0.5 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                        Chữ ký:
                      </Typography>
                      <Box
                        component="img"
                        src={h.signature_base64_png}
                        alt="Signature"
                        sx={{
                          width: "100%",
                          maxWidth: 520,
                          border: "1px solid",
                          borderColor: "divider",
                          borderRadius: 2,
                          bgcolor: "background.paper",
                        }}
                      />
                    </Box>
                  ) : null}
                </Stack>
              </Paper>
            ))}
          </Stack>
        )}
      </Paper>

      <Dialog open={open} onClose={() => (busy ? null : setOpen(false))} maxWidth="sm" fullWidth>
        <DialogTitle>Xác nhận thao tác</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Alert severity="info">
              Thao tác: <b>{action}</b>. Bạn có thể ký trực tiếp vào ô bên dưới (chuột/touch). Khi bấm “Xác nhận”, hệ thống sẽ lưu chữ ký kèm lịch sử xử lý.
            </Alert>
            <TextField
              label="Ghi chú"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              multiline
              minRows={2}
              fullWidth
              helperText={action === "return" ? "Bắt buộc: ghi rõ lý do trả lại hồ sơ." : "Tùy chọn"}
            />
            <SignaturePad value={signature} onChange={setSignature} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} disabled={busy}>
            Hủy
          </Button>
          <Button
            variant="contained"
            disabled={busy || !row || (action === "return" && !note.trim())}
            onClick={async () => {
              if (!row) return;
              setBusy(true);
              try {
                const res = await api.post<Dossier>(`/dossiers/${row.id}/actions`, {
                  action,
                  note,
                  signature_base64_png: signature?.substring(0, 255),
                });
                setRow(res.data);
                setOpen(false);
              } catch (e: any) {
                alert(e?.response?.data?.detail ?? e?.message ?? "Thao tác thất bại");
              } finally {
                setBusy(false);
              }
            }}
          >
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}


