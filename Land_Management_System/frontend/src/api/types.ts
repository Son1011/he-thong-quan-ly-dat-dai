export type UserRole =
  | "ADMIN"
  | "COMMUNE_OFFICER"
  | "PROVINCE_OFFICER"
  | "CENTRAL_OFFICER";

export type DossierStatus = "PENDING" | "APPROVED" | "RETURNED" | "ESCALATED";

export type Me = {
  id: number;
  username: string;
  role: UserRole;
  unit_id: number;
  must_change_password: boolean;
  password_expires_at?: string | null;
};

export type Dossier = {
  id: number;
  title: string;
  status: DossierStatus;
  sent_to_central?: boolean;
  origin_unit_id: number;
  created_by_user_id: number;
  assigned_to_unit_id: number;
  created_at: string;
  updated_at: string;
};

export type UnitLevel = "CENTRAL" | "PROVINCE" | "COMMUNE";
export type UnitKind = "COMMUNE" | "WARD";

export type Unit = {
  id: number;
  name: string;
  level: UnitLevel;
  kind?: UnitKind | null;
  parent_id?: number | null;
};


