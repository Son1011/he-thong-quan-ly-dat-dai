import {
  Alert,
  Box,
  Button,
  Grid,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
  IconButton,
} from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Dossier } from "../api/types";
import { StatusChip } from "../components/StatusChip";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import DeleteIcon from "@mui/icons-material/Delete";

export default function NewDossierPage() {
  const nav = useNavigate();

  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [created, setCreated] = useState<Dossier | null>(null);

  const [files, setFiles] = useState<File[]>([]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dossierTypeId, setDossierTypeId] = useState("1");
  const [originUnitId, setOriginUnitId] = useState("3");

  const [citizenName, setCitizenName] = useState("");
  const [citizenPhone, setCitizenPhone] = useState("");
  const [citizenIdentityNumber, setCitizenIdentityNumber] = useState("");
  const [citizenAddress, setCitizenAddress] = useState("");

  const [errors, setErrors] = useState<{
    citizenIdentityNumber?: string;
    citizenPhone?: string;
  }>({});

  // ================= FILE =================
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) setFiles(Array.from(e.target.files));
  };

  const removeFile = (index: number) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  // ================= VALIDATE =================
  const validate = () => {
    const newErrors: any = {};

    if (citizenIdentityNumber.length !== 12) {
      newErrors.citizenIdentityNumber = "CCCD phải đúng 12 chữ số";
    }

    if (citizenPhone.length !== 10) {
      newErrors.citizenPhone = "SĐT phải đúng 10 chữ số";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // ================= SUBMIT =================
  const handleSubmit = async () => {
    setBusy(true);
    setErr(null);

    if (!validate()) {
      setBusy(false);
      return;
    }

    try {
      const payload = {
        title: title.trim(),
        description: description.trim(),
        origin_unit_id: Number(originUnitId),
        dossier_type_id: Number(dossierTypeId),
        citizen_name: citizenName.trim(),
        citizen_identity_number: citizenIdentityNumber.trim(),
        citizen_phone: citizenPhone.trim(),
        citizen_address: citizenAddress.trim(),
      };

      const res = await api.post<Dossier>("/dossiers", payload);
      const newDossier = res.data;
      setCreated(newDossier);

      if (files.length > 0) {
        for (const file of files) {
          const fd = new FormData();
          fd.append("file", file);

          await api.post(`/dossiers/${newDossier.id}/attachments`, fd, {
            headers: { "Content-Type": "multipart/form-data" },
          });
        }
      }

      nav(`/dossiers/${newDossier.id}`);
    } catch (e: any) {
      const data = e?.response?.data;

      if (e?.response?.status === 400 && data) {
        if (data.errors) {
          setErrors((prev) => ({ ...prev, ...data.errors }));
        }
        setErr(data.detail || "Dữ liệu không hợp lệ");
      } else {
        setErr(e?.message || "Tạo hồ sơ thất bại");
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <Stack spacing={2}>
      <Typography variant="h6" fontWeight={700}>
        Tạo hồ sơ mới
      </Typography>

      <Paper sx={{ p: 2, borderRadius: 2 }}>
        <Stack spacing={2}>
          {err && <Alert severity="error">{err}</Alert>}
          {created && (
            <Alert severity="success">
              Đã tạo #{created.id} · <StatusChip status={created.status} />
            </Alert>
          )}

          <Grid container spacing={1.5}>
            {/* THÔNG TIN HỒ SƠ */}
            <Grid item xs={12}>
              <Typography fontWeight={600} color="primary">
                Thông tin hồ sơ
              </Typography>
            </Grid>

            <Grid item xs={12} md={8}>
              <TextField
                label="Tiêu đề"
                fullWidth
                size="small"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                select
                label="Loại"
                fullWidth
                size="small"
                value={dossierTypeId}
                onChange={(e) => setDossierTypeId(e.target.value)}
              >
                <MenuItem value="1">Cấp đất</MenuItem>
                <MenuItem value="2">Chuyển nhượng</MenuItem>
              </TextField>
            </Grid>

            <Grid item xs={12} md={8}>
              <TextField
                label="Mô tả"
                fullWidth
                size="small"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                label="Đơn vị"
                fullWidth
                size="small"
                value={originUnitId}
                onChange={(e) => setOriginUnitId(e.target.value)}
              />
            </Grid>

            {/* CÔNG DÂN */}
            <Grid item xs={12}>
              <Typography fontWeight={600} color="primary">
                Thông tin công dân
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Họ tên"
                fullWidth
                size="small"
                value={citizenName}
                onChange={(e) => setCitizenName(e.target.value)}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="CCCD"
                fullWidth
                size="small"
                value={citizenIdentityNumber}
                error={!!errors.citizenIdentityNumber}
                helperText={errors.citizenIdentityNumber}
                inputProps={{ maxLength: 12 }}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "");
                  setCitizenIdentityNumber(value);
                  setErrors((p) => ({
                    ...p,
                    citizenIdentityNumber: undefined,
                  }));
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Điện thoại"
                fullWidth
                size="small"
                value={citizenPhone}
                error={!!errors.citizenPhone}
                helperText={errors.citizenPhone}
                inputProps={{ maxLength: 10 }}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, "");
                  setCitizenPhone(value);
                  setErrors((p) => ({
                    ...p,
                    citizenPhone: undefined,
                  }));
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Địa chỉ"
                fullWidth
                size="small"
                value={citizenAddress}
                onChange={(e) => setCitizenAddress(e.target.value)}
              />
            </Grid>

            {/* FILE */}
            <Grid item xs={12}>
              <Typography fontWeight={600} color="primary">
                Tài liệu đính kèm
              </Typography>

              <Button
                component="label"
                variant="outlined"
                size="small"
                startIcon={<CloudUploadIcon />}
                sx={{ mt: 1 }}
              >
                Chọn file
                <input hidden type="file" multiple onChange={handleFileChange} />
              </Button>

              {files.length > 0 && (
                <Stack spacing={0.5} mt={1}>
                  {files.map((file, i) => (
                    <Box
                      key={i}
                      sx={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        bgcolor: "grey.50",
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 1,
                      }}
                    >
                      <Typography variant="body2" noWrap>
                        {file.name}
                      </Typography>

                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => removeFile(i)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ))}
                </Stack>
              )}
            </Grid>
          </Grid>

          {/* BUTTONS */}
          <Stack direction="row" justifyContent="flex-end" spacing={1.5}>
            <Button onClick={() => nav("/dossiers")} disabled={busy}>
              Hủy
            </Button>

            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={busy}
            >
              {busy ? "Đang tạo..." : "Tạo hồ sơ"}
            </Button>
          </Stack>
        </Stack>
      </Paper>
    </Stack>
  );
}