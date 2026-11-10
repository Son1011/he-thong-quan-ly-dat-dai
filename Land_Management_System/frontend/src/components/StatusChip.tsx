import { Chip } from "@mui/material";
import type { DossierStatus } from "../api/types";

export function statusLabel(status: DossierStatus): string {
  switch (status) {
    case "PENDING":
      return "Chờ duyệt";
    case "APPROVED":
      return "Đã duyệt";
    case "ESCALATED":
      return "Đã gửi cấp trên";
    case "RETURNED":
      return "Bị trả lại";
    default:
      return status;
  }
}

export function StatusChip({ status }: { status: DossierStatus }) {
  const color =
    status === "PENDING"
      ? "error"
      : status === "APPROVED"
        ? "success"
        : status === "ESCALATED"
          ? "warning"
          : "default";

  return (
    <Chip
      size="small"
      label={statusLabel(status)}
      color={color}
      variant="outlined"
      sx={{ fontWeight: 700 }}
    />
  );
}


