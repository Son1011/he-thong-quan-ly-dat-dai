import { Box, Button, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { useEffect, useMemo, useRef, useState } from "react";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
  Title,
} from "chart.js";
import { api } from "../api/client";
import type { Unit } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import VietnamInteractiveMap from "../components/VietnamInteractiveMap";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend, Title);

type StatRow = {
  unit_id: number;
  name: string;
  total_dossiers: number;
  pending: number;
  approved: number;
  returned: number;
  total_attachments: number;
};

export default function StatisticsPage() {
  const { me } = useAuth();
  const [scope, setScope] = useState<"DOSSIERS" | "ATTACHMENTS">("DOSSIERS");
  const [provinceId, setProvinceId] = useState<number | "">("");
  const [provinces, setProvinces] = useState<Unit[]>([]);
  const [provinceRows, setProvinceRows] = useState<StatRow[]>([]); // always province-level (for map)
  const [rows, setRows] = useState<StatRow[]>([]);
  const [loading, setLoading] = useState(true);
  const chartRef = useRef<ChartJS<"bar"> | null>(null);
  const reportRef = useRef<HTMLDivElement | null>(null);
  const [mapMetric, setMapMetric] = useState<"pending" | "approved" | "returned" | "total_dossiers" | "total_attachments">("pending");

  useEffect(() => {
    const run = async () => {
      if (!me) return;
      setLoading(true);
      try {
        if (me.role === "CENTRAL_OFFICER") {
          const p = await api.get<Unit[]>("/units/provinces");
          setProvinces(p.data);
          const s = await api.get<StatRow[]>("/stats/provinces");
          setProvinceRows(s.data);
          setRows(s.data);
        } else if (me.role === "PROVINCE_OFFICER") {
          setProvinceId(me.unit_id);
          const s = await api.get<StatRow[]>(`/stats/provinces/${me.unit_id}/children`);
          setRows(s.data);
        } else {
          setRows([]);
        }
      } finally {
        setLoading(false);
      }
    };
    void run();
  }, [me]);

  const loadChildren = async (pid: number) => {
    setLoading(true);
    try {
      const s = await api.get<StatRow[]>(`/stats/provinces/${pid}/children`);
      setRows(s.data);
    } finally {
      setLoading(false);
    }
  };

  const loadProvincesStats = async () => {
    const s = await api.get<StatRow[]>("/stats/provinces");
    setProvinceRows(s.data);
    setRows(s.data);
  };

  const setProvinceAndLoad = async (pid: number | "") => {
    setProvinceId(pid);
    if (pid === "") {
      // prefer cached provinceRows if present to keep UI snappy
      if (provinceRows.length) {
        setRows(provinceRows);
      } else {
        await loadProvincesStats();
      }
    } else {
      await loadChildren(pid);
    }
  };

  const labels = useMemo(() => rows.map((r) => r.name), [rows]);
  const data = useMemo(() => {
    if (scope === "ATTACHMENTS") {
      return {
        labels,
        datasets: [
          {
            label: "Tài liệu đính kèm",
            data: rows.map((r) => r.total_attachments),
            backgroundColor: "rgba(21, 101, 192, 0.75)",
          },
        ],
      };
    }
    return {
      labels,
      datasets: [
        {
          label: "Chưa duyệt/Đang xử lý",
          data: rows.map((r) => r.pending),
          backgroundColor: "rgba(211, 47, 47, 0.75)",
        },
        {
          label: "Bị trả lại",
          data: rows.map((r) => r.returned),
          backgroundColor: "rgba(97, 97, 97, 0.75)",
        },
        {
          label: "Đã duyệt",
          data: rows.map((r) => r.approved),
          backgroundColor: "rgba(46, 125, 50, 0.75)",
        },
      ],
    };
  }, [labels, rows, scope]);

  const title = useMemo(() => {
    if (!me) return "Thống kê";
    if (me.role === "CENTRAL_OFFICER") return "Thống kê (Trung ương)";
    if (me.role === "PROVINCE_OFFICER") return "Thống kê (Cấp tỉnh)";
    return "Thống kê";
  }, [me]);

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h5" fontWeight={900}>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Trung ương xem biểu đồ theo tỉnh và có thể drill xuống xã/phường. Tỉnh xem biểu đồ theo xã/phường thuộc tỉnh.
        </Typography>
      </Box>

      {!me || (me.role !== "CENTRAL_OFFICER" && me.role !== "PROVINCE_OFFICER") ? (
        <Paper sx={{ p: 2 }}>
          <Typography>Bạn không có quyền xem thống kê.</Typography>
        </Paper>
      ) : (
        <>
          <Paper sx={{ p: 2 }}>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
              <TextField
                select
                size="small"
                label="Loại biểu đồ"
                value={scope}
                onChange={(e) => {
                  const v = e.target.value as any;
                  setScope(v);
                  // default map metric mapping
                  if (v === "ATTACHMENTS") setMapMetric("total_attachments");
                  else setMapMetric("pending");
                }}
                sx={{ minWidth: 220 }}
              >
                <MenuItem value="DOSSIERS">Hồ sơ (theo trạng thái)</MenuItem>
                <MenuItem value="ATTACHMENTS">Tài liệu đính kèm</MenuItem>
              </TextField>

              {me.role === "CENTRAL_OFFICER" ? (
                <TextField
                  select
                  size="small"
                  label="Tô màu bản đồ theo"
                  value={mapMetric}
                  onChange={(e) => setMapMetric(e.target.value as any)}
                  sx={{ minWidth: 220 }}
                >
                  <MenuItem value="pending">Chưa duyệt/Đang xử lý (pending)</MenuItem>
                  <MenuItem value="approved">Đã duyệt (approved)</MenuItem>
                  <MenuItem value="returned">Bị trả lại (returned)</MenuItem>
                  <MenuItem value="total_dossiers">Tổng hồ sơ (total)</MenuItem>
                  <MenuItem value="total_attachments">Tổng tài liệu đính kèm</MenuItem>
                </TextField>
              ) : null}

              {me.role === "CENTRAL_OFFICER" ? (
                <TextField
                  select
                  size="small"
                  label="Xem theo"
                  value={provinceId}
                  onChange={async (e) => {
                    const v = e.target.value === "" ? "" : Number(e.target.value);
                    await setProvinceAndLoad(v);
                  }}
                  sx={{ minWidth: 280 }}
                >
                  <MenuItem value="">34 tỉnh/thành</MenuItem>
                  {provinces.map((p) => (
                    <MenuItem key={p.id} value={p.id}>
                      {p.name}
                    </MenuItem>
                  ))}
                </TextField>
              ) : null}

              <Box sx={{ flexGrow: 1 }} />
              {me.role === "CENTRAL_OFFICER" && provinceId !== "" ? (
                <Button variant="outlined" onClick={async () => setProvinceAndLoad("")}>
                  Quay lại 34 tỉnh/thành
                </Button>
              ) : null}
              <Button
                variant="outlined"
                disabled={loading || rows.length === 0}
                onClick={() => {
                  const chart = chartRef.current;
                  if (!chart) return;
                  const url = chart.toBase64Image("image/png", 1);
                  const a = document.createElement("a");
                  const name =
                    me.role === "CENTRAL_OFFICER"
                      ? provinceId === ""
                        ? "bao-cao-thong-ke-34-tinh.png"
                        : `bao-cao-thong-ke-${provinceId}.png`
                      : "bao-cao-thong-ke-xa-phuong.png";
                  a.href = url;
                  a.download = name;
                  a.click();
                }}
              >
                Xuất ảnh (PNG)
              </Button>
              <Button
                variant="contained"
                disabled={loading || rows.length === 0}
                onClick={async () => {
                  const el = reportRef.current;
                  if (!el) return;
                  const canvas = await html2canvas(el, { scale: 2, backgroundColor: "#ffffff" });
                  const imgData = canvas.toDataURL("image/png");

                  const pdf = new jsPDF({ orientation: "portrait", unit: "mm", format: "a4" });
                  const pageW = pdf.internal.pageSize.getWidth();
                  const pageH = pdf.internal.pageSize.getHeight();

                  // Fit image to A4 while preserving aspect ratio
                  const imgW = pageW;
                  const imgH = (canvas.height * imgW) / canvas.width;
                  let y = 10;

                  if (imgH <= pageH - 20) {
                    pdf.addImage(imgData, "PNG", 0, y, imgW, imgH);
                  } else {
                    // Multi-page: slice vertically
                    let remainingPx = canvas.height;
                    let offsetPx = 0;
                    while (remainingPx > 0) {
                      const slicePx = Math.min(remainingPx, Math.floor((canvas.width * (pageH - 20)) / pageW));
                      const sliceCanvas = document.createElement("canvas");
                      sliceCanvas.width = canvas.width;
                      sliceCanvas.height = slicePx;
                      const ctx = sliceCanvas.getContext("2d");
                      if (!ctx) break;
                      ctx.drawImage(canvas, 0, offsetPx, canvas.width, slicePx, 0, 0, canvas.width, slicePx);
                      const sliceData = sliceCanvas.toDataURL("image/png");
                      const sliceH = (slicePx * pageW) / canvas.width;
                      pdf.addImage(sliceData, "PNG", 0, y, pageW, sliceH);
                      remainingPx -= slicePx;
                      offsetPx += slicePx;
                      if (remainingPx > 0) pdf.addPage();
                    }
                  }

                  const name =
                    me.role === "CENTRAL_OFFICER"
                      ? provinceId === ""
                        ? "bao-cao-thong-ke-34-tinh.pdf"
                        : `bao-cao-thong-ke-${provinceId}.pdf`
                      : "bao-cao-thong-ke-xa-phuong.pdf";
                  pdf.save(name);
                }}
              >
                Xuất PDF
              </Button>
            </Stack>
          </Paper>

          <Box ref={reportRef}>
            {me.role === "CENTRAL_OFFICER" ? (
              <Paper sx={{ p: 2, mb: 2 }}>
                <VietnamInteractiveMap
                  provinces={provinces}
                  rows={provinceRows}
                  selectedProvinceId={provinceId}
                  metric={mapMetric}
                  onSelectProvinceId={async (pid) => {
                    await setProvinceAndLoad(pid);
                  }}
                  title="Bản đồ Việt Nam (click tỉnh để drill xuống)"
                />
              </Paper>
            ) : null}

            <Paper sx={{ p: 2 }}>
              {loading ? (
                <Typography>Đang tải...</Typography>
              ) : rows.length === 0 ? (
                <Typography>Chưa có dữ liệu thống kê.</Typography>
              ) : (
                <Bar
                  ref={(instance: unknown) => {
                    chartRef.current = instance as unknown as ChartJS<"bar"> | null;
                  }}
                  data={data as any}
                  options={{
                    responsive: true,
                    plugins: {
                      legend: { position: "top" as const },
                      title: { display: false, text: "" },
                    },
                    scales: { x: { ticks: { maxRotation: 45, minRotation: 0 } } },
                  }}
                />
              )}
            </Paper>
          </Box>
        </>
      )}
    </Stack>
  );
}


