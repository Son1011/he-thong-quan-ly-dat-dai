import { Box, Typography } from "@mui/material";
import React, { useEffect, useMemo, useRef, useState } from "react";
import type { Unit } from "../api/types";

type StatRow = {
  unit_id: number;
  name: string;
  total_dossiers: number;
  pending: number;
  approved: number;
  returned: number;
  total_attachments: number;
};

type Metric = "pending" | "approved" | "returned" | "total_dossiers" | "total_attachments";

function lerp(a: number, b: number, t: number) {
  return a + (b - a) * t;
}

function clamp01(x: number) {
  return Math.max(0, Math.min(1, x));
}

function colorScale(v: number, min: number, max: number) {
  if (!Number.isFinite(v)) return "#e5e7eb";
  if (max <= min) return "#93c5fd";
  const t = clamp01((v - min) / (max - min));
  const r = Math.round(lerp(219, 30, t));
  const g = Math.round(lerp(234, 64, t));
  const b = Math.round(lerp(254, 175, t));
  return `rgb(${r},${g},${b})`;
}

/**
 * 👉 BẢNG ĐỐI CHIẾU DANH MỤC CHUẨN
 * Khớp class SVG về đúng Tên chuẩn của API để lấy dữ liệu thống kê chính xác
 */
function getStandardApiName(rawString: string): string | null {
  if (!rawString) return null;
  const str = rawString.toLowerCase().trim();

  // Khu vực Miền Bắc (15 Tỉnh/Thành phố)
  if (str.includes("ha noi") || str.includes("hà nội")) return "Thành phố Hà Nội";
  if (str.includes("hai phong") || str.includes("hải phòng")) return "Thành phố Hải Phòng";
  if (str.includes("bac ninh") || str.includes("bắc ninh")) return "Tỉnh Bắc Ninh";
  if (str.includes("hung yen") || str.includes("hưng yên")) return "Tỉnh Hưng Yên";
  if (str.includes("ninh binh") || str.includes("ninh bình")) return "Tỉnh Ninh Bình";
  if (str.includes("phu tho") || str.includes("phú thọ")) return "Tỉnh Phú Thọ";
  if (str.includes("quang ninh") || str.includes("quảng ninh")) return "Tỉnh Quảng Ninh";
  if (str.includes("son la") || str.includes("sơn la")) return "Tỉnh Sơn La";
  if (str.includes("thai nguyen") || str.includes("thái nguyên")) return "Tỉnh Thái Nguyên";
  if (str.includes("tuyen quang") || str.includes("tuyên quang")) return "Tỉnh Tuyên Quang";
  if (str.includes("cao bang") || str.includes("cao bằng")) return "Tỉnh Cao Bằng";
  if (str.includes("dien bien") || str.includes("điện biên")) return "Tỉnh Điện Biên";
  if (str.includes("lai chau") || str.includes("lai châu")) return "Tỉnh Lai Châu";
  if (str.includes("lang son") || str.includes("lạng sơn")) return "Tỉnh Lạng Sơn";
  if (str.includes("lao cai") || str.includes("lào cai")) return "Tỉnh Lào Cai";

  // Khu vực Miền Trung & Tây Nguyên (11 Tỉnh/Thành phố)
  if (str.includes("da nang") || str.includes("đà nẵng")) return "Thành phố Đà Nẵng";
  if (str.includes("thua thien") || str.includes("hue") || str.includes("huế") || str.includes("hế")) return "Thành phố Huế";
  if (str.includes("ha tinh") || str.includes("hà tĩnh")) return "Tỉnh Hà Tĩnh";
  if (str.includes("nghe an") || str.includes("nghệ an")) return "Tỉnh Nghệ An";
  if (str.includes("quang ngai") || str.includes("quảng ngãi")) return "Tỉnh Quảng Ngãi";
  if (str.includes("quang tri") || str.includes("quảng trị")) return "Tỉnh Quảng Trị";
  if (str.includes("thanh hoa") || str.includes("thanh hóa")) return "Tỉnh Thanh Hóa";
  if (str.includes("khanh hoa") || str.includes("khánh hòa")) return "Tỉnh Khánh Hòa";
  if (str.includes("lam dong") || str.includes("lâm đồng")) return "Tỉnh Lâm Đồng";
  if (str.includes("gia lai")) return "Tỉnh Gia Lai";
  if (str.includes("dak lak") || str.includes("đắk lắk") || str.includes("dac lac")) return "Tỉnh Đắk Lắk";

  // Khu vực Miền Nam (8 Tỉnh/Thành phố)
  if (str.includes("ho chi minh") || str.includes("hồ chí minh") || str.includes("hcm")) return "Thành phố Hồ Chí Minh";
  if (str.includes("can tho") || str.includes("cần thơ")) return "Thành phố Cần Thơ";
  if (str.includes("an giang")) return "Tỉnh An Giang";
  if (str.includes("ca mau") || str.includes("cà mau")) return "Tỉnh Cà Mau";
  if (str.includes("dong nai") || str.includes("đồng nai")) return "Tỉnh Đồng Nai";
  if (str.includes("dong thap") || str.includes("đồng tháp")) return "Tỉnh Đồng Tháp";
  if (str.includes("tay ninh") || str.includes("tây ninh")) return "Tỉnh Tây Ninh";
  if (str.includes("vinh long") || str.includes("vĩnh long")) return "Tỉnh Vĩnh Long";

  return null;
}

export default function VietnamInteractiveMap({
  provinces,
  rows,
  selectedProvinceId,
  metric,
  onSelectProvinceId,
  title = "Bản đồ Việt Nam (tương tác)",
}: {
  provinces: Unit[];
  rows: StatRow[];
  selectedProvinceId: number | "";
  metric: Metric;
  onSelectProvinceId: (provinceId: number) => void;
  title?: string;
}) {
  const wrapperRef = useRef<HTMLDivElement | null>(null);
  const svgHostRef = useRef<HTMLDivElement | null>(null);
  const [svg, setSvg] = useState<string | null>(null);
  const [hover, setHover] = useState<null | { name: string; value: number; hasData: boolean; x: number; y: number }>(
    null,
  );

  // Ánh xạ dữ liệu hàng thống kê (rows) dựa trên tên chuẩn của API
  const rowsByStandardName = useMemo(() => {
    const m = new Map<string, StatRow>();
    for (const r of rows) {
      if (r?.name) {
        const stdName = getStandardApiName(r.name);
        if (stdName) m.set(stdName, r);
      }
    }
    return m;
  }, [rows]);

  // Ánh xạ danh sách Tỉnh/ID từ API danh mục (Bỏ qua Tỉnh A)
  const provinceByStandardName = useMemo(() => {
    const m = new Map<string, Unit>();
    for (const p of provinces) {
      if (p?.name && p.name.trim() !== "Tỉnh A") {
        const stdName = getStandardApiName(p.name);
        if (stdName) m.set(stdName, p);
      }
    }
    return m;
  }, [provinces]);

  const range = useMemo(() => {
    const vals = rows.map((r) => Number((r as any)[metric] ?? 0)).filter((n) => Number.isFinite(n));
    if (!vals.length) return { min: 0, max: 0 };
    return { min: Math.min(...vals), max: Math.max(...vals) };
  }, [rows, metric]);

  useEffect(() => {
    let cancelled = false;
    const run = async () => {
      try {
        const res = await fetch("/maps/vn-provinces.svg");
        const text = await res.text();
        if (!cancelled) setSvg(text);
      } catch {
        if (!cancelled) setSvg(null);
      }
    };
    void run();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const el = svgHostRef.current;
    if (!el) return;
    const svgEl = el.querySelector("svg");
    if (!svgEl) return;

    const labelCircles = Array.from(svgEl.querySelectorAll<SVGCircleElement>("#label_points circle[class]"));
    const circles =
      labelCircles.length > 0
        ? labelCircles
        : Array.from(svgEl.querySelectorAll<SVGCircleElement>("circle[class]")).filter((c) => {
            const cls = c.getAttribute("class") ?? "";
            return cls && !cls.includes("|");
          });
    const listeners: Array<() => void> = [];

    for (const c of circles) {
      const rawClassName = c.getAttribute("class") ?? "";
      const stdName = getStandardApiName(rawClassName);
      const matchedProvince = stdName ? provinceByStandardName.get(stdName) : null;

      if (!matchedProvince) {
        c.style.display = "none";
        continue;
      }

      // ÉP KIỂU NUMBER: Đảm bảo ID luôn luôn là kiểu số chuẩn chỉnh của API
      const pid = Number(matchedProvince.id);
      const row = rowsByStandardName.get(stdName!);

      if (!c.getAttribute("r")) c.setAttribute("r", "7");
      c.style.display = "block";

      const value = row ? Number((row as any)[metric] ?? 0) : 0;
      const fill = row ? colorScale(value, range.min, range.max) : "#e5e7eb";

      c.style.fill = fill;
      c.style.stroke = "#111827";
      c.style.strokeWidth = "1";
      c.style.cursor = "pointer";

      // So sánh chính xác sau khi đã ép kiểu Number
      if (selectedProvinceId !== "" && Number(selectedProvinceId) === pid) {
        c.style.strokeWidth = "2.5";
        c.style.stroke = "#1d4ed8"; 
        c.setAttribute("r", "9");
      } else {
        c.setAttribute("r", "7");
      }

      c.setAttribute("data-tooltip", matchedProvince.name);
      c.setAttribute(
        "title",
        `${matchedProvince.name}${row ? ` — ${metric}: ${value}` : " (Chưa có số liệu)"}`
      );

      // 🛠️ HÀM CLICK ĐÃ ĐƯỢC THÊM LOG DEBUG
      const onClick = () => {
        console.log("=== MAP INTERACTION ===");
        console.log("👉 Bạn vừa click tỉnh:", matchedProvince.name);
        console.log("👉 ID gửi lên Component Cha (onSelectProvinceId):", pid, `(Kiểu dữ liệu: ${typeof pid})`);
        console.log("👉 Dữ liệu Row thống kê tương ứng tìm thấy trong Map:", row);
        
        // Gọi hàm truyền dữ liệu lên cha
        onSelectProvinceId(pid);
      };

      c.addEventListener("click", onClick);
      listeners.push(() => c.removeEventListener("click", onClick));
    }

    return () => listeners.forEach((fn) => fn());
  }, [metric, onSelectProvinceId, provinceByStandardName, range.max, range.min, rowsByStandardName, selectedProvinceId, svg]);

  return (
    <Box>
      <Typography fontWeight={900} sx={{ mb: 1 }}>
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
        Hover để xem số liệu, click để chọn tỉnh.
      </Typography>
      {!svg ? (
        <Typography variant="body2" color="text.secondary">
          Không tải được bản đồ SVG.
        </Typography>
      ) : (
        <Box
          ref={wrapperRef}
          sx={{
            width: "100%",
            maxWidth: 720,
            mx: "auto",
            position: "relative",
          }}
          onMouseMove={(e) => {
            const t = e.target as any;
            const rect = wrapperRef.current?.getBoundingClientRect();
            if (!t || typeof t.getAttribute !== "function") {
              setHover(null);
              return;
            }
            if ((t as Element).tagName?.toLowerCase() !== "circle") {
              setHover(null);
              return;
            }
            const rawClassName = t.getAttribute("class") ?? "";
            const stdName = getStandardApiName(rawClassName);
            const matchedProvince = stdName ? provinceByStandardName.get(stdName) : null;
            
            if (!matchedProvince) {
              setHover(null);
              return;
            }

            const row = rowsByStandardName.get(stdName!);
            const value = row ? Number((row as any)[metric] ?? 0) : 0;
            const x = rect ? e.clientX - rect.left : 0;
            const y = rect ? e.clientY - rect.top : 0;

            setHover({ name: matchedProvince.name, value, hasData: Boolean(row), x, y });
          }}
          onMouseLeave={() => setHover(null)}
        >
          <Box
            ref={svgHostRef}
            sx={{
              "& svg": { width: "100%", height: "auto", display: "block" },
            }}
            dangerouslySetInnerHTML={{ __html: svg }}
          />

          {hover ? (
            <Box
              sx={{
                position: "absolute",
                left: hover.x,
                top: hover.y,
                transform: "translate(12px, 12px)",
                bgcolor: "rgba(17,24,39,0.92)",
                color: "#fff",
                px: 1,
                py: 0.75,
                borderRadius: 1,
                fontSize: 12,
                lineHeight: 1.25,
                pointerEvents: "none",
                maxWidth: 260,
                zIndex: 2,
                boxShadow: "0 10px 30px rgba(0,0,0,0.25)",
              }}
            >
              <div style={{ fontWeight: 700 }}>{hover.name}</div>
              <div>
                {metric}: <b>{hover.value}</b>
                {!hover.hasData ? " (chưa có số liệu)" : ""}
              </div>
            </Box>
          ) : null}
        </Box>
      )}
      <Typography variant="caption" color="text.secondary">
        Tip: click vào dấu tròn trên tỉnh để drill xuống thống kê xã/phường.
      </Typography>
    </Box>
  );
}