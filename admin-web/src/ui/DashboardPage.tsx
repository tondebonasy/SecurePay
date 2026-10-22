import React, { useEffect, useState } from "react";
import { StatusBadge } from "./StatusBadge";
import { Icon } from "./Icon";
import OperationsWorkspace from "./OperationsWorkspace";
import { api } from "../util/api";
import {
  Typography,
  Button,
  Card,
  CardContent,
  GridLegacy as Grid,
  Box,
  CircularProgress,
  Alert,
  TextField,
  MenuItem,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  TableContainer,
} from "@mui/material";

export default function DashboardPage() {
  return <OperationsWorkspace users={<Users />} invites={<Invites />} />;
}

function Users() {
  const [search, setSearch] = useState("");
  const [users, setUsers] = useState<any[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    phone: "",
    email: "",
    name: "",
    password: "",
    role: "user",
  });

  async function load() {
    setErr(null);
    try {
      const resp = await api.get("/v1/admin/users");
      setUsers(resp.data?.data || []);
    } catch (e: any) {
      setErr(e?.response?.data?.error?.message || "Failed");
    }
  }

  async function create() {
    setBusy(true);
    try {
      await api.post("/v1/admin/users", form);
      setForm({ phone: "", email: "", name: "", password: "", role: "user" });
      await load();
    } catch (e: any) {
      setErr(e?.response?.data?.error?.message || "Failed");
    } finally {
      setBusy(false);
    }
  }

  async function toggleStatus(id: string, status: string) {
    if (status === "DELETED") return;
    const next = status === "ACTIVE" ? "DISABLED" : "ACTIVE";
    try {
      await api.patch(`/v1/admin/users/${id}`, { status: next });
      await load();
    } catch (e: any) {
      setErr(e?.response?.data?.error?.message || "Update failed");
    }
  }

  async function deleteUser(id: string) {
    if (
      !window.confirm(
        "Delete this user? Their login access will be revoked and personal details anonymized.",
      )
    ) {
      return;
    }
    setErr(null);
    try {
      await api.delete(`/v1/admin/users/${id}`);
      await load();
    } catch (e: any) {
      setErr(e?.response?.data?.error?.message || "Failed");
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <Card>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          Customer directory
        </Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>
          Manage customer accounts, access, and account status.
        </Typography>
        <Box className="customer-create">
          <Typography variant="subtitle2" sx={{ mb: 2 }}>
            Create an account
          </Typography>
          <Grid container spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <Grid item>
              <TextField
                label="Phone"
                size="small"
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
              />
            </Grid>
            <Grid item>
              <TextField
                label="Email"
                size="small"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </Grid>
            <Grid item>
              <TextField
                label="Name"
                size="small"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </Grid>
            <Grid item>
              <TextField
                label="Password"
                type="password"
                size="small"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </Grid>
            <Grid item>
              <TextField
                select
                label="Role"
                size="small"
                value={form.role}
                onChange={(e) => setForm({ ...form, role: e.target.value })}
              >
                <MenuItem value="user">Standard user</MenuItem>
                <MenuItem value="admin">Administrator</MenuItem>
              </TextField>
            </Grid>
            <Grid item>
              <Button variant="contained" onClick={create} disabled={busy}>
                {busy ? <CircularProgress size={24} /> : "Create"}
              </Button>
            </Grid>
          </Grid>
        </Box>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            gap: 2,
            my: 3,
          }}
        >
          <Typography variant="subtitle2">{users.length} accounts</Typography>
          <TextField
            size="small"
            label="Search customers"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </Box>
        {err && <Alert severity="error">{err}</Alert>}

        {users.length > 0 && (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  {["Customer", "Contact", "Access", "Status", "Joined"].map(
                    (c) => (
                      <TableCell key={c}>{c}</TableCell>
                    ),
                  )}
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users
                  .filter((u) =>
                    [u.displayName, u.email, u.phone, u.id].some((v) =>
                      String(v || "")
                        .toLowerCase()
                        .includes(search.toLowerCase()),
                    ),
                  )
                  .map((u, i) => (
                    <TableRow key={u.id || i}>
                      <TableCell>
                        <Box
                          sx={{
                            display: "flex",
                            gap: 1.5,
                            alignItems: "center",
                          }}
                        >
                          <Box className="customer-avatar">
                            {(u.displayName || "C")
                              .split(" ")
                              .slice(0, 2)
                              .map((n: string) => n[0])
                              .join("")}
                          </Box>
                          <Box>
                            <Typography fontSize={12} fontWeight={650}>
                              {u.displayName || "Unnamed customer"}
                            </Typography>
                            <Typography color="text.secondary" fontSize={10}>
                              {u.id?.slice(0, 8)}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography fontSize={12}>
                          {u.email || "No email address"}
                        </Typography>
                        <Typography color="text.secondary" fontSize={11}>
                          {u.phone}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography fontSize={11}>
                          {String(u.roles).includes("ADMIN")
                            ? "Administrator"
                            : "Standard user"}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <StatusBadge status={u.status} />
                      </TableCell>
                      <TableCell>
                        {new Date(u.createdAt).toLocaleDateString(undefined, {
                          day: "numeric",
                          month: "short",
                          year: "numeric",
                        })}
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={() => toggleStatus(u.id, u.status)}
                            disabled={u.status === "DELETED"}
                          >
                            {u.status === "ACTIVE" ? "Disable" : "Enable"}
                          </Button>
                          <Button
                            variant="outlined"
                            color="error"
                            size="small"
                            onClick={() => deleteUser(u.id)}
                            disabled={u.status === "DELETED"}
                          >
                            Delete
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </CardContent>
    </Card>
  );
}

function Invites() {
  const [ttlMinutes, setTtlMinutes] = useState("60");
  const [invite, setInvite] = useState<{
    code: string;
    expiresAt: string;
  } | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function createInvite() {
    setErr(null);
    setInvite(null);
    setBusy(true);
    try {
      const resp = await api.post("/v1/admin/invites", {
        ttlMinutes: Number(ttlMinutes) || 60,
      });
      setInvite(resp.data?.data);
    } catch (e: any) {
      setErr(e?.response?.data?.error?.message || "Failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card sx={{ maxWidth: 780 }}>
      <CardContent>
        <Box className="section-icon">
          <Icon name="Administration" size={24} />
        </Box>
        <Typography variant="h5" gutterBottom>
          Administrator invitations
        </Typography>
        <Typography color="text.secondary" gutterBottom>
          Create one-time codes to grant admin access.
        </Typography>

        <TextField
          label="Invite TTL (minutes)"
          value={ttlMinutes}
          onChange={(e) => setTtlMinutes(e.target.value)}
          sx={{ mt: 2, mb: 2 }}
        />
        <br />
        <Button variant="contained" onClick={createInvite} disabled={busy}>
          {busy ? <CircularProgress size={24} /> : "Create Invite"}
        </Button>
        {err && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {err}
          </Alert>
        )}
        {invite && (
          <Card sx={{ mt: 2 }}>
            <CardContent>
              <Typography>
                <strong>Code:</strong> {invite.code}
              </Typography>
              <Typography>
                <strong>Expires:</strong> {invite.expiresAt}
              </Typography>
            </CardContent>
          </Card>
        )}
      </CardContent>
    </Card>
  );
}

function Cell({ v }: { v: any }) {
  if (v === null || v === undefined)
    return (
      <Typography component="span" color="text.secondary">
        —
      </Typography>
    );
  if (typeof v === "object")
    return (
      <pre style={{ whiteSpace: "pre-wrap", margin: 0, maxWidth: 420 }}>
        {JSON.stringify(v, null, 2)}
      </pre>
    );
  const s = String(v);
  if (s.length > 80) return <span title={s}>{s.slice(0, 80)}…</span>;
  return <span>{s}</span>;
}
