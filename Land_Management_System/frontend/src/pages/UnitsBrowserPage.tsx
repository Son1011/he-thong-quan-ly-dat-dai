import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import type { Dossier, Unit } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import { StatusChip } from "../components/StatusChip";
import { Link as RouterLink } from "react-router-dom";
import { useNavigate } from "react-router-dom";


type PaginatedResponse<T> = {
  data: T[];
  total: number;
  page: number;
  size: number;
  total_pages: number;
  has_next: boolean;
  has_previous: boolean;
};

export default function UnitsBrowserPage() {
  console.log("UNITS PAGE LOADED");
  const { me } = useAuth();
  const nav = useNavigate();
  const [provinces, setProvinces] = useState<Unit[]>([]);
  const [children, setChildren] = useState<Unit[]>([]);
  const [selectedProvince, setSelectedProvince] = useState<Unit | null>(null);
  const [selectedChild, setSelectedChild] = useState<Unit | null>(null);
  const [rows, setRows] = useState<Dossier[]>([]);
  const [loading, setLoading] = useState(true);
  const [provinceQuery, setProvinceQuery] = useState("");
  const [childQuery, setChildQuery] = useState("");

  const title = useMemo(() => {
    if (!me) return "Đơn vị";
    if (me.role === "CENTRAL_OFFICER") return "Danh sách tỉnh/thành";
    if (me.role === "PROVINCE_OFFICER") return "Danh sách xã/phường";
    return "Đơn vị";
  }, [me]);

  useEffect(() => {
    const run = async () => {
      if (!me) return;
      setLoading(true);
      try {
        if (me.role === "CENTRAL_OFFICER") {
          const res = await api.get<Unit[]>("/units/provinces");
          setProvinces(res.data);
        } else if (me.role === "PROVINCE_OFFICER") {
          const res = await api.get<Unit[]>(`/units/${me.unit_id}/children`);
          setChildren(res.data);
          // auto load dossiers for whole province (include children), but exclude those sent to central (shown in separate menu)
          const d = await api.get<PaginatedResponse<Dossier>>("/dossiers", {
  params: { unit_id: me.unit_id, include_children: true, sent_to_central: false },
});

console.log("Dossiers response:", d.data);
setRows(d.data.data ?? []);
        }
      } finally {
        setLoading(false);
      }
    };
    void run();
  }, [me]);

  const loadProvince = async (p: Unit) => {
    setSelectedProvince(p);
    setSelectedChild(null);
    setLoading(true);
    try {
      const res = await api.get<Unit[]>(`/units/${p.id}/children`);
      setChildren(res.data);
      // Central is all-powerful: show ALL dossiers in that province (including those sent to central).
      // Province officer uses a dedicated "sent to central" screen, so we exclude them only at province level.
      const d = await api.get<PaginatedResponse<Dossier>>("/dossiers", {
  params:
    me?.role === "PROVINCE_OFFICER"
      ? { unit_id: p.id, include_children: true, sent_to_central: false }
      : { unit_id: p.id, include_children: true },
});

console.log("Dossiers response:", d.data);
setRows(d.data.data ?? []);
    } finally {
      setLoading(false);
    }
  };

  const loadChild = async (u: Unit) => {
    setSelectedChild(u);
    setLoading(true);
    try {
      const d = await api.get<PaginatedResponse<Dossier>>("/dossiers", {
  params:
    me?.role === "PROVINCE_OFFICER"
      ? { unit_id: u.id, include_children: false, sent_to_central: false }
      : { unit_id: u.id, include_children: false },
});

console.log("Dossiers response:", d.data);
setRows(d.data.data ?? []);
    }finally {
    setLoading(false);
  }
};
  const filteredProvinces = useMemo(() => {
    const q = provinceQuery.trim().toLowerCase();
    if (!q) return provinces;
    return provinces.filter((p) => p.name.toLowerCase().includes(q));
  }, [provinces, provinceQuery]);

  const filteredChildren = useMemo(() => {
    const q = childQuery.trim().toLowerCase();
    if (!q) return children;
    return children.filter((c) => c.name.toLowerCase().includes(q));
  }, [children, childQuery]);

  const pendingRows = useMemo(() => rows.filter((d) => d.status === "PENDING" || d.status === "ESCALATED"), [rows]);
  const returnedRows = useMemo(() => rows.filter((d) => d.status === "RETURNED"), [rows]);
  const approvedRows = useMemo(() => rows.filter((d) => d.status === "APPROVED"), [rows]);

  const originName = useMemo(() => {
    const m = new Map<number, string>();
    for (const c of children) m.set(c.id, c.name);
    if (selectedProvince) m.set(selectedProvince.id, "Hồ sơ do tỉnh tạo");
    if (me?.role === "PROVINCE_OFFICER") m.set(me.unit_id, "Hồ sơ do tỉnh tạo");
    return (originId: number) => m.get(originId) ?? `Đơn vị #${originId}`;
  }, [children, selectedProvince, me]);

  const groupByOrigin = (list: Dossier[]) => {
    const map = new Map<string, Dossier[]>();
    for (const d of list) {
      const key = originName(d.origin_unit_id);
      map.set(key, [...(map.get(key) ?? []), d]);
    }
    return Array.from(map.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([k, v]) => ({ title: k, items: v }));
  };

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h5" fontWeight={800}>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Click tỉnh → xem các xã/phường và hồ sơ. Click xã/phường → lọc hồ sơ theo đơn vị đó.
        </Typography>
      </Box>

      {!me || (me.role !== "CENTRAL_OFFICER" && me.role !== "PROVINCE_OFFICER") ? (
        <Alert severity="info">Chức năng này chỉ dành cho cấp tỉnh và trung ương.</Alert>
      ) : (
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="stretch">
          <Paper sx={{ p: 2, width: { md: 360 } }}>
            <Typography fontWeight={800} sx={{ mb: 1 }}>
              {me.role === "CENTRAL_OFFICER" ? "34 tỉnh/thành" : "Xã / Phường"}
            </Typography>
            <TextField
              size="small"
              placeholder={me.role === "CENTRAL_OFFICER" ? "Tìm tỉnh..." : "Tìm xã/phường..."}
              value={me.role === "CENTRAL_OFFICER" ? provinceQuery : childQuery}
              onChange={(e) => (me.role === "CENTRAL_OFFICER" ? setProvinceQuery(e.target.value) : setChildQuery(e.target.value))}
              fullWidth
              sx={{ mb: 1 }}
            />
            {loading && provinces.length === 0 && children.length === 0 ? (
              <Typography>Đang tải...</Typography>
            ) : me.role === "CENTRAL_OFFICER" ? (
              <Stack spacing={1}>
                {filteredProvinces.map((p) => (
                  <Button
                    key={p.id}
                    variant={selectedProvince?.id === p.id ? "contained" : "outlined"}
                    onClick={() => void loadProvince(p)}
                    sx={{ justifyContent: "flex-start" }}
                  >
                    {p.name}
                  </Button>
                ))}
              </Stack>
            ) : (
              <Stack spacing={1}>
                {filteredChildren.map((u) => (
                  <Button
                    key={u.id}
                    variant={selectedChild?.id === u.id ? "contained" : "outlined"}
                    onClick={() => void loadChild(u)}
                    sx={{ justifyContent: "flex-start" }}
                  >
                    {u.name}
                  </Button>
                ))}
              </Stack>
            )}
          </Paper>

          <Paper sx={{ p: 2, flex: 1 }}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
              <Typography fontWeight={800}>
                Hồ sơ
                {selectedChild ? ` · ${selectedChild.name}` : selectedProvince ? ` · ${selectedProvince.name}` : ""}
              </Typography>
              <Box sx={{ flexGrow: 1 }} />
              {me.role === "CENTRAL_OFFICER" && selectedProvince ? (
                <Button size="small" variant="outlined" onClick={() => void loadProvince(selectedProvince)}>
                  Tải lại
                </Button>
              ) : null}
            </Stack>

            {loading ? (
              <Typography>Đang tải...</Typography>
            ) : rows.length === 0 ? (
              <Typography>Chưa có hồ sơ.</Typography>
            ) : (
              <Stack spacing={2}>
                <Box>
                  <Typography fontWeight={800} sx={{ mb: 1 }}>
                    Hồ sơ chưa duyệt
                  </Typography>
                  {pendingRows.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
                      Không có.
                    </Typography>
                  ) : (
                    <Stack spacing={1}>
                      {groupByOrigin(pendingRows).map((g) => (
                        <Box key={g.title}>
                          <Typography variant="subtitle2" sx={{ mb: 1 }}>
                            {g.title}
                          </Typography>
                          <Stack spacing={1}>
                            {g.items.map((d) => (
                              <Paper
                                key={d.id}
                                variant="outlined"
                                sx={{ p: 2, borderColor: "error.main", cursor: "pointer" }}
                                onClick={() => nav(`/dossiers/${d.id}`)}
                              >
                                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                                  <Box sx={{ flexGrow: 1 }}>
                                    <Typography fontWeight={800}>{d.title}</Typography>
                                  </Box>
                                  <StatusChip status={d.status} />
                                </Stack>
                              </Paper>
                            ))}
                          </Stack>
                        </Box>
                      ))}
                    </Stack>
                  )}
                </Box>
                <Box>
                  <Typography fontWeight={800} sx={{ mb: 1 }}>
                    Hồ sơ bị trả lại
                  </Typography>
                  {returnedRows.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
                      Không có.
                    </Typography>
                  ) : (
                    <Stack spacing={1}>
                      {groupByOrigin(returnedRows).map((g) => (
                        <Box key={g.title}>
                          <Typography variant="subtitle2" sx={{ mb: 1 }}>
                            {g.title}
                          </Typography>
                          <Stack spacing={1}>
                            {g.items.map((d) => (
                              <Paper
                                key={d.id}
                                variant="outlined"
                                sx={{ p: 2, borderColor: "grey.500", cursor: "pointer" }}
                                onClick={() => nav(`/dossiers/${d.id}`)}
                              >
                                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                                  <Box sx={{ flexGrow: 1 }}>
                                    <Typography fontWeight={800}>{d.title}</Typography>
                                  </Box>
                                  <StatusChip status={d.status} />
                                </Stack>
                              </Paper>
                            ))}
                          </Stack>
                        </Box>
                      ))}
                    </Stack>
                  )}
                </Box>
                <Box>
                  <Typography fontWeight={800} sx={{ mb: 1 }}>
                    Hồ sơ đã duyệt
                  </Typography>
                  {approvedRows.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
                      Không có.
                    </Typography>
                  ) : (
                    <Stack spacing={1}>
                      {groupByOrigin(approvedRows).map((g) => (
                        <Box key={g.title}>
                          <Typography variant="subtitle2" sx={{ mb: 1 }}>
                            {g.title}
                          </Typography>
                          <Stack spacing={1}>
                            {g.items.map((d) => (
                              <Paper
                                key={d.id}
                                variant="outlined"
                                sx={{ p: 2, borderColor: "success.main", cursor: "pointer" }}
                                onClick={() => nav(`/dossiers/${d.id}`)}
                              >
                                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                                  <Box sx={{ flexGrow: 1 }}>
                                    <Typography fontWeight={800}>{d.title}</Typography>
                                  </Box>
                                  <StatusChip status={d.status} />
                                </Stack>
                              </Paper>
                            ))}
                          </Stack>
                        </Box>
                      ))}
                    </Stack>
                  )}
                </Box>
              </Stack>
            )}

            {me.role === "CENTRAL_OFFICER" && selectedProvince ? (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>
                  Xã / Phường thuộc {selectedProvince.name}
                </Typography>
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                  {children.map((u) => (
                    <Button key={u.id} size="small" variant="outlined" onClick={() => void loadChild(u)}>
                      {u.name}
                    </Button>
                  ))}
                </Stack>
              </Box>
            ) : null}
          </Paper>
        </Stack>
      )}
    </Stack>
  );
}

  
