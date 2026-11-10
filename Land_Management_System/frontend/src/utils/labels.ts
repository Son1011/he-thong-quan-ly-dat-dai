import type { UserRole } from "../api/types";

export function roleLabel(role: UserRole): string {
  switch (role) {
    case "ADMIN":
      return "Quản trị";
    case "CENTRAL_OFFICER":
      return "Cán bộ Trung ương";
    case "PROVINCE_OFFICER":
      return "Cán bộ Tỉnh";
    case "COMMUNE_OFFICER":
      return "Cán bộ Xã/Phường";
    default:
      return role;
  }
}


