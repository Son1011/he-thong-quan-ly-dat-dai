import React, { useEffect, useRef, useState } from "react";
import { Box, Button, Stack, Typography } from "@mui/material";

type Props = {
  value: string | null;
  onChange: (base64Png: string | null) => void;
  height?: number;
};

export default function SignaturePad({ value, onChange, height = 160 }: Props) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);

  useEffect(() => {
    const c = canvasRef.current;
    if (!c) return;
    const context = c.getContext("2d");
    if (!context) return;
    context.lineWidth = 2;
    context.lineCap = "round";
    context.strokeStyle = "#111827";
    ctxRef.current = context;
  }, []);

  const clear = () => {
    const c = canvasRef.current;
    const context = c?.getContext("2d");
    if (!c || !context) return;
    context.clearRect(0, 0, c.width, c.height);
    onChange(null);
  };

  const exportPng = () => {
    const c = canvasRef.current;
    if (!c) return;
    const png = c.toDataURL("image/png");
    onChange(png);
  };

  useEffect(() => {
    // When value is set externally, best-effort preview.
    if (!value) return;
    const c = canvasRef.current;
    const context = c?.getContext("2d");
    if (!c || !context) return;
    const img = new Image();
    img.onload = () => {
      context.clearRect(0, 0, c.width, c.height);
      context.drawImage(img, 0, 0);
    };
    img.src = value;
  }, [value]);

  const getPoint = (e: React.PointerEvent<HTMLCanvasElement>) => {
    const rect = (e.target as HTMLCanvasElement).getBoundingClientRect();
    return { x: e.clientX - rect.left, y: e.clientY - rect.top };
  };

  return (
    <Box>
      <Typography variant="subtitle2" sx={{ mb: 1 }}>
        Chữ ký (đơn giản)
      </Typography>
      <Box
        sx={{
          border: "1px solid",
          borderColor: "divider",
          borderRadius: 2,
          bgcolor: "background.paper",
          overflow: "hidden",
        }}
      >
        <canvas
          ref={canvasRef}
          width={900}
          height={height}
          style={{ width: "100%", height, display: "block", touchAction: "none" }}
          onPointerDown={(e) => {
            const ctx = ctxRef.current;
            if (!ctx) return;
            const p = getPoint(e);
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            setIsDrawing(true);
          }}
          onPointerMove={(e) => {
            const ctx = ctxRef.current;
            if (!ctx || !isDrawing) return;
            const p = getPoint(e);
            ctx.lineTo(p.x, p.y);
            ctx.stroke();
          }}
          onPointerUp={() => {
            setIsDrawing(false);
            // Auto-save to reduce confusion; still keep manual "Lưu chữ ký" button.
            exportPng();
          }}
          onPointerLeave={() => setIsDrawing(false)}
        />
      </Box>
      <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
        <Button size="small" variant="outlined" onClick={clear}>
          Xóa
        </Button>
        <Button size="small" variant="contained" onClick={exportPng}>
          Lưu chữ ký
        </Button>
      </Stack>
    </Box>
  );
}


