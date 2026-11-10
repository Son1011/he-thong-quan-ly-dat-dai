import {
  Box,
  Paper,
  Stack,
  Typography,
  Button,
  TextField,
  MenuItem,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { Link as RouterLink } from "react-router-dom";
import { api } from "../api/client";
import type { Dossier } from "../api/types";
import { StatusChip } from "../components/StatusChip";

export default function InboxPage() {
  const [rows, setRows] = useState<Dossier[]>([]);
  const [loading, setLoading] = useState(true);

  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<"ALL" | string>("ALL");

  useEffect(() => {
    const run = async () => {
      setLoading(true);

      const res = await api.get("/dossiers/inbox");

      // hỗ trợ mọi kiểu response BE
      const data = Array.isArray(res.data)
        ? res.data
        : res.data?.data ?? res.data?.content ?? [];

      setRows(data);
      setLoading(false);
    };

    void run();
  }, []);

  const filtered = useMemo(() => {
    const keyword = search.trim().toLowerCase();

    return rows.filter((d: any) => {
      const title = (d.title ?? "").toLowerCase();
      const id = String(d.id);

      // ===== SEARCH (id + title, không phân biệt hoa thường)
      const matchSearch =
        keyword === "" ||
        title.includes(keyword) ||
        id.includes(keyword);

      // ===== FIX FIELD BACKEND (quan trọng)
      const dossierTypeId =
        d.dossierTypeId ?? d.dossier_type_id ?? d.dossierType?.id;

      const matchType =
        typeFilter === "ALL"
          ? true
          : String(dossierTypeId) === typeFilter;

      return matchSearch && matchType;
    });
  }, [rows, search, typeFilter]);

  const reset = () => {
    setSearch("");
    setTypeFilter("ALL");
  };

  return (
    <Stack spacing={2}>
      {/* HEADER */}
      <Box>
        <Typography variant="h5" fontWeight={800}>
          Hồ sơ cần xử lý
        </Typography>
      </Box>

      {/* SEARCH + FILTER */}
      <Paper sx={{ p: 1.5 }}>
        <Stack direction="row" spacing={1} alignItems="center">

          {/* SEARCH*/}
          <TextField
            size="small"
            label="Tìm kiếm (tên / ID)"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            sx={{ width: 180 }}   
          />

          {/* FILTER TYPE */}
          <TextField
            select
            size="small"
            label="Loại hồ sơ"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            sx={{ width: 200 }}
          >
            <MenuItem value="ALL">Tất cả</MenuItem>
            <MenuItem value="1">Cấp giấy chứng nhận quyền sử dụng đất</MenuItem>
            <MenuItem value="2">Chuyển nhượng đất</MenuItem>
            <MenuItem value="3">Tách thửa / hợp thửa</MenuItem>
            <MenuItem value="4">Cấp lại giấy tờ</MenuItem>
            <MenuItem value="5">Cấp đất</MenuItem>
          </TextField>

          <Box sx={{ flexGrow: 1 }} />

          {/* RESET */}
          <Button size="small" variant="outlined" onClick={reset}>
            Tải lại
          </Button>
        </Stack>
      </Paper>

      {/* LIST */}
      <Paper sx={{ p: 2 }}>
        {loading ? (
          <Typography>Đang tải...</Typography>
        ) : filtered.length === 0 ? (
          <Typography>Không có hồ sơ.</Typography>
        ) : (
          <Stack spacing={1}>
            {filtered.map((d: any) => {
              const dossierTypeId =
                d.dossierTypeId ?? d.dossier_type_id ?? d.dossierType?.id;

              return (
                <Paper key={d.id} variant="outlined" sx={{ p: 1.5 }}>
                  <Stack direction="row" spacing={2} alignItems="center">
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography fontWeight={600}>
                        {d.title}
                      </Typography>

                      <Typography variant="caption" color="text.secondary">
                        ID #{d.id} · type #{dossierTypeId}
                      </Typography>
                    </Box>

                    <StatusChip status={d.status} />

                    <Button
                      size="small"
                      variant="contained"
                      component={RouterLink}
                      to={`/dossiers/${d.id}`}
                    >
                      Mở
                    </Button>
                  </Stack>
                </Paper>
              );
            })}
          </Stack>
        )}
      </Paper>
    </Stack>
  );
}