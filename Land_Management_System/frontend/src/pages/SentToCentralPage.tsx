import { Box, Paper, Stack, TextField, Typography } from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import type { Dossier, Unit } from "../api/types";
import { useAuth } from "../auth/AuthContext";
import { StatusChip } from "../components/StatusChip";

type Group = { title: string; items: Dossier[] };

function groupByOrigin(items: Dossier[], getName: (originId: number) => string): Group[] {
  const map = new Map<string, Dossier[]>();
  for (const d of items) {
    const key = getName(d.origin_unit_id);
    map.set(key, [...(map.get(key) ?? []), d]);
  }
  return Array.from(map.entries())
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([title, list]) => ({ title, items: list }));
}

export default function SentToCentralPage() {
  const nav = useNavigate();
  const { me } = useAuth();
  const [children, setChildren] = useState<Unit[]>([]);
  const [rows, setRows] = useState<Dossier[]>([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const run = async () => {
      if (!me) return;
      setLoading(true);
      try {
        if (me.role !== "PROVINCE_OFFICER") return;
        const units = await api.get<Unit[]>(`/units/${me.unit_id}/children`);
        setChildren(units.data);
        const d = await api.get<Dossier[]>("/dossiers", {
          params: { unit_id: me.unit_id, include_children: true, sent_to_central: true },
        });
        setRows(d.data);
      } finally {
        setLoading(false);
      }
    };
    void run();
  }, [me]);

  const nameMap = useMemo(() => {
    const m = new Map<number, string>();
    for (const c of children) m.set(c.id, c.name);
    if (me) m.set(me.unit_id, "Hồ sơ do tỉnh tạo");
    return m;
  }, [children, me]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return rows;
    return rows.filter((d) => d.title.toLowerCase().includes(q));
  }, [rows, query]);

const returned = useMemo(
  () => (Array.isArray(filtered) ? filtered : []).filter((d) => d.status === "RETURNED"),
  [filtered]
);

const approved = useMemo(
  () => (Array.isArray(filtered) ? filtered : []).filter((d) => d.status === "APPROVED"),
  [filtered]
);

const inProgress = useMemo(
  () =>
    (Array.isArray(filtered) ? filtered : []).filter(
      (d) => d.status !== "RETURNED" && d.status !== "APPROVED"
    ),
  [filtered]
);

  const getOriginName = (originId: number) => nameMap.get(originId) ?? `Đơn vị #${originId}`;

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h5" fontWeight={900}>
          Hồ sơ gửi lên Trung ương
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Gồm cả hồ sơ tỉnh gửi lên và hồ sơ xã/phường đã được tỉnh chuyển lên Trung ương. Khi Trung ương duyệt/trả về, tỉnh vẫn theo dõi tại đây.
        </Typography>
      </Box>

      <Paper sx={{ p: 2 }}>
        <TextField
          size="small"
          fullWidth
          label="Tìm theo tiêu đề hồ sơ"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </Paper>

      <Paper sx={{ p: 2 }}>
        {loading ? (
          <Typography>Đang tải...</Typography>
        ) : rows.length === 0 ? (
          <Typography>Chưa có hồ sơ gửi lên Trung ương.</Typography>
        ) : (
          <Stack spacing={2}>
            <Box>
              <Typography fontWeight={900} sx={{ mb: 1 }}>
                Đang xử lý / chờ duyệt
              </Typography>
              {inProgress.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  Không có.
                </Typography>
              ) : (
                <Stack spacing={1}>
                  {groupByOrigin(inProgress, getOriginName).map((g) => (
                    <Box key={g.title}>
                      <Typography variant="subtitle2" sx={{ mb: 1 }}>
                        {g.title}
                      </Typography>
                      <Stack spacing={1}>
                        {g.items.map((d) => (
                          <Paper
                            key={d.id}
                            variant="outlined"
                            sx={{ p: 2, cursor: "pointer" }}
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
              <Typography fontWeight={900} sx={{ mb: 1 }}>
                Bị trả lại
              </Typography>
              {returned.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  Không có.
                </Typography>
              ) : (
                <Stack spacing={1}>
                  {groupByOrigin(returned, getOriginName).map((g) => (
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
              <Typography fontWeight={900} sx={{ mb: 1 }}>
                Đã duyệt
              </Typography>
              {approved.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  Không có.
                </Typography>
              ) : (
                <Stack spacing={1}>
                  {groupByOrigin(approved, getOriginName).map((g) => (
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
      </Paper>
    </Stack>
  );
}


