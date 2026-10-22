import React, { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { api } from "../util/api";
export default function Profile() {
  const [name, setName] = useState(""),
    [email, setEmail] = useState(""),
    [roles, setRoles] = useState("");
  const [error, setError] = useState(""),
    [saved, setSaved] = useState(false),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    api
      .get("/v1/auth/me")
      .then((r) => {
        const d = r.data.data;
        setName(d.displayName || "");
        setEmail(d.email || "");
        setRoles(d.roles.join(", "));
      })
      .catch(() => setError("Unable to load profile."));
  }, []);
  async function save(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setSaved(false);
    setError("");
    try {
      await api.patch("/v1/auth/me", { displayName: name, email });
      setSaved(true);
    } catch (e: any) {
      setError(e.response?.data?.error?.message || "Profile update failed");
    } finally {
      setBusy(false);
    }
  }
  return (
    <Card sx={{ maxWidth: 780 }}>
      <CardContent>
        <Box className="profile-heading">
          <Box className="profile-avatar">
            {name
              ? name
                  .split(" ")
                  .slice(0, 2)
                  .map((n) => n[0])
                  .join("")
              : "SP"}
          </Box>
          <Box>
            <Typography variant="h5">Your profile</Typography>
            <Typography color="text.secondary" fontSize={12}>
              Keep your account details up to date.
            </Typography>
          </Box>
        </Box>
        <Typography color="text.secondary" sx={{ mb: 3 }}>
          Roles: {roles}
        </Typography>
        <Stack
          component="form"
          gap={2.5}
          sx={{ maxWidth: 520 }}
          onSubmit={save}
        >
          <TextField
            required
            label="Display name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            inputProps={{ maxLength: 120 }}
          />
          <TextField
            type="email"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <Button
            type="submit"
            variant="contained"
            disabled={busy || !name.trim()}
          >
            {busy ? "Saving…" : "Save profile"}
          </Button>
          {error && <Alert severity="error">{error}</Alert>}
          {saved && <Alert severity="success">Profile saved.</Alert>}
        </Stack>
      </CardContent>
    </Card>
  );
}
