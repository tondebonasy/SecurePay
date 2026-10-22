import { TransactionTable } from "../components/TransactionTable";

import React, { useContext, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  MenuItem,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TextField,
  Typography,
  IconButton,
  InputAdornment,
} from "@mui/material";
import { api } from "../util/api";
import { clearAuth, getAuth } from "../util/auth";
import { ThemeMode } from "../themeMode";
import Profile from "./Profile";
import { StatusBadge } from "./StatusBadge";
import { Icon } from "./Icon";
import "../polish.css";
const FinancialChart = React.lazy(() =>
  import("../components/FinancialChart").then((module) => ({
    default: module.FinancialChart,
  })),
);
const sections = [
  "Overview",
  "Transactions",
  "Verification",
  "Reconciliation",
  "Refunds",
  "Risk indicators",
  "Customers",
  "Administration",
  "Reports",
  "Settings",
];
const message = (e: any) =>
  e?.response?.data?.error?.message ||
  "Unable to complete this request. Please retry.";
const money = (v: number | string, c: string) =>
  new Intl.NumberFormat(undefined, { style: "currency", currency: c }).format(
    Number(v),
  );
export default function OperationsWorkspace({
  users,
  invites,
}: {
  users: React.ReactNode;
  invites: React.ReactNode;
}) {
  const [page, setPage] = useState("Overview"),
    [currency, setCurrency] = useState("USD"),
    [from, setFrom] = useState(""),
    [to, setTo] = useState(""),
    [mobile, setMobile] = useState(false),
    [headerSearch, setHeaderSearch] = useState("");
  const { mode, toggle } = useContext(ThemeMode);
  useEffect(() => {
    if (!mobile) return;
    const previous = document.activeElement as HTMLElement | null;
    const nav = document.querySelector<HTMLElement>(".workspace-sidebar");
    const controls = Array.from(
      nav?.querySelectorAll<HTMLElement>("button") || [],
    ).filter((el) => getComputedStyle(el).display !== "none");
    const first = controls[0],
      last = controls[controls.length - 1];
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    first?.focus();
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setMobile(false);
      if (e.key === "Tab" && controls.length) {
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = overflow;
      previous?.focus();
    };
  }, [mobile]);
  const dates = {
    currency,
    ...(from ? { from: new Date(from + "T00:00:00Z").toISOString() } : {}),
    ...(to
      ? {
          to: new Date(
            new Date(to + "T00:00:00Z").getTime() + 86400000,
          ).toISOString(),
        }
      : {}),
  };
  return (
    <Box className="workspace-app" sx={{ display: "flex", minHeight: "100vh" }}>
      {mobile && (
        <button
          className="workspace-backdrop"
          aria-label="Close navigation"
          onClick={() => setMobile(false)}
        />
      )}
      <Box
        className={"workspace-sidebar " + (mobile ? "is-open" : "")}
        component="nav"
        aria-label="Main navigation"
        sx={{
          width: { xs: "100%", md: 264 },
          flexShrink: 0,
          bgcolor: "#13233B",
          color: "#CBD5E1",
          p: { xs: 0.75, md: 2 },
          position: "fixed",
          height: { xs: 74, md: "100vh" },
          top: { xs: "auto", md: 0 },
          bottom: { xs: 0, md: "auto" },
          left: 0,
          zIndex: 20,
          display: "block",
        }}
      >
        <Button
          className="mobile-nav-close"
          aria-label="Close menu"
          onClick={() => setMobile(false)}
        >
          <Icon name="close" />
        </Button>
        <Stack direction="row" spacing={1.25} alignItems="center" sx={{ mb: 3, px: 1, pt: 1 }}>
          <img src="/securepay.svg" alt="" width="38" height="38" />
          <Box>
            <Typography variant="h6" color="white" fontWeight={750} sx={{ lineHeight: 1.1 }}>SecurePay</Typography>
            <Typography sx={{ color: "#7E91B2", fontSize: 10, mt: 0.4 }}>Operations workspace</Typography>
          </Box>
        </Stack>
        <Box className="workspace-switcher">
          <span className="workspace-switcher-mark">SP</span>
          <span><strong>Primary workspace</strong><small>Settlement and payments</small></span>
          <Icon name="chevron" size={15} />
        </Box>
        <Typography variant="overline" sx={{ px: 1, mt: 3, color: "#6F83A6" }}>
          OPERATIONS
        </Typography>
        {sections.map((s) => (
          <Button
            key={s}
            className="workspace-nav-item"
            data-mobile-hidden={!['Overview', 'Transactions', 'Verification', 'Reports', 'Settings'].includes(s) ? "true" : undefined}
            fullWidth
            onClick={() => {
              setPage(s);
              setMobile(false);
            }}
            aria-current={page === s ? "page" : undefined}
            sx={{
              justifyContent: "flex-start",
              color: page === s ? "white" : "#B8C7DF",
              bgcolor: page === s ? "#20365E" : "transparent",
              mb: 0.6,
              py: 1.2,
              "&:hover": { bgcolor: "#20365E" },
            }}
          >
            <Icon name={s} size={18} />
            {s}
          </Button>
        ))}
        <Box className="workspace-sidebar-footer">
          <Box className="sidebar-account">
            <span className="account-avatar">SP</span>
            <Box>
              <strong>Operations workspace</strong>
              <small>SecurePay management console</small>
            </Box>
          </Box>
          <Divider sx={{ my: 2, borderColor: "#ffffff10" }} />
          <Typography variant="caption" sx={{ px: 1, color: "#94A3B8" }}>
            Admin console
          </Typography>
        </Box>
      </Box>
      <Box className="workspace-content" sx={{ flex: 1, minWidth: 0, ml: { md: "264px" } }}>
        <Box
          className="workspace-header"
          component="header"
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: 1,
            px: { xs: 2, md: 4 },
            py: 2,
            bgcolor: "background.paper",
            borderBottom: "1px solid",
            borderColor: "divider",
          }}
        >
          <Box className="workspace-breadcrumb">
            <Typography color="text.secondary" variant="body2">Workspace</Typography>
            <span>/</span>
            <Typography color="text.primary" variant="body2" fontWeight={650}>{page}</Typography>
          </Box>
          <Stack className="workspace-header-tools" direction="row" spacing={1} alignItems="center">
            <TextField
              className="workspace-search"
              size="small"
              placeholder="Search transactions"
              value={headerSearch}
              onChange={(event) => setHeaderSearch(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") setPage("Transactions");
              }}
              inputProps={{ "aria-label": "Search transactions" }}
              InputProps={{
                startAdornment: <InputAdornment position="start"><Icon name="search" size={16} /></InputAdornment>,
              }}
            />
            <IconButton aria-label="Notifications" className="workspace-icon-button">
              <Icon name="bell" size={18} />
            </IconButton>
            <Button
              aria-label={mode === "light" ? "Dark theme" : "Light theme"}
              onClick={toggle}
              startIcon={
                <Icon name={mode === "light" ? "moon" : "sun"} size={16} />
              }
            >
              {mode === "light" ? "Dark" : "Light"} theme
            </Button>
            <IconButton aria-label="Profile" className="workspace-profile-button">
              <span>SP</span>
            </IconButton>
            <Button
              className="workspace-signout"
              aria-label="Sign out"
              onClick={async () => {
                const auth = getAuth();
                try {
                  if (auth)
                    await api.post("/v1/auth/logout", {
                      sessionId: auth.sessionId,
                      refreshToken: auth.refreshToken,
                    });
                } finally {
                  clearAuth();
                  location.href = "/login";
                }
              }}
            >
              <Icon name="logout" size={16} />
              <Box component="span" sx={{ ml: 1 }}>Sign out</Box>
            </Button>
          </Stack>
        </Box>
        <Box
          className="workspace-main"
          component="main"
          sx={{ p: { xs: 2, md: 4 }, maxWidth: 1600, mx: "auto" }}
        >
          <Stack
            className="page-heading workspace-page-heading"
            direction="row"
            justifyContent="space-between"
            alignItems="center"
            sx={{ mb: 3 }}
          >
            <Box>
              <Typography className="page-eyebrow" component="p">OPERATIONS OVERVIEW</Typography>
              <Typography variant="h4" component="h1">
                {page}
              </Typography>
              <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                Selected period performance and payment records.
              </Typography>
            </Box>
            <Box className="page-heading-meta">
              <span className="page-sync-label">Selected period</span>
            </Box>
          </Stack>

          {["Overview", "Transactions", "Reports"].includes(page) && (
            <Stack
              className="reporting-filters"
              direction="row"
              gap={2}
              flexWrap="wrap"
              sx={{ mb: 3 }}
            >
              <TextField
                select
                label="Currency"
                size="small"
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
              >
                {["USD", "EUR", "GBP", "ZWG", "ZAR"].map((c) => (
                  <MenuItem value={c} key={c}>
                    {c}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="From (UTC)"
                type="date"
                size="small"
                InputLabelProps={{ shrink: true }}
                value={from}
                onChange={(e) => setFrom(e.target.value)}
              />
              <TextField
                label="Through (UTC)"
                type="date"
                size="small"
                InputLabelProps={{ shrink: true }}
                value={to}
                onChange={(e) => setTo(e.target.value)}
              />
            </Stack>
          )}
          {(page === "Overview" || page === "Reports") && (
            <Overview params={dates} />
          )}
          {page === "Transactions" && <Transactions params={dates} />}
          {page === "Settings" && <Profile />}
          {page === "Customers" && users}
          {page === "Administration" && invites}
          {[
            "Verification",
            "Refunds",
            "Reconciliation",
            "Risk indicators",
          ].includes(page) && <Workflow key={page} page={page} />}
          {page === "Reports" && (
            <Button
              sx={{ mt: 2 }}
              variant="outlined"
              onClick={() => window.print()}
            >
              Print report / save as PDF
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
}
function Overview({ params }: { params: any }) {
  const [data, setData] = useState<any>(null),
    [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    setData(null);
    setError("");
    api
      .get("/v1/admin/operations/analytics", { params })
      .then((r) => {
        if (active) setData(r.data.data);
      })
      .catch((e) => {
        if (active) setError(message(e));
      });
    return () => {
      active = false;
    };
  }, [JSON.stringify(params)]);
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!data)
    return (
      <Stack direction="row" gap={2}>
        {[1, 2, 3].map((n) => (
          <Skeleton key={n} variant="rounded" width="33%" height={130} />
        ))}
      </Stack>
    );
  const dailyEntries = Object.entries(data.daily || {})
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([day, value]) => ({ day, value: Number(value) || 0 }));
  const latestDaily = dailyEntries.at(-1)?.value || 0;
  const recentWindow = dailyEntries.slice(-7).reduce((sum, item) => sum + item.value, 0);
  const previousWindow = dailyEntries.slice(-14, -7).reduce((sum, item) => sum + item.value, 0);
  const change = previousWindow > 0 ? ((recentWindow - previousWindow) / previousWindow) * 100 : null;
  const sparklineMax = Math.max(...dailyEntries.slice(-10).map((item) => item.value), 1);
  const summaries = [
    { label: "Total collected this term", value: money(data.totalValue, data.currency), note: `${data.totalTx} recorded transactions`, trend: change },
    { label: "Today's receipts", value: money(latestDaily, data.currency), note: dailyEntries.at(-1)?.day || "No dated receipts", trend: change },
    { label: "Collection rate", value: `${Number(data.successRate || 0).toFixed(1)}%`, note: `${data.successful} successful of ${data.totalTx}`, trend: null },
  ];
  return (
    <>
      <Box className="overview-summary-grid">
        {summaries.map((summary) => (
          <Card key={summary.label} className="overview-summary-card">
            <CardContent>
              <Typography className="overview-summary-label">{summary.label}</Typography>
              <Typography className="overview-summary-value">{summary.value}</Typography>
              <Box className="overview-summary-meta">
                {summary.trend === null ? null : summary.trend === 0 ? (
                  <span className="overview-summary-neutral">No change</span>
                ) : (
                  <span className={summary.trend > 0 ? "overview-summary-positive" : "overview-summary-negative"}>
                    {summary.trend > 0 ? "↑" : "↓"} {Math.abs(summary.trend).toFixed(1)}%
                  </span>
                )}
                <span className="overview-summary-note">{summary.note}</span>
              </Box>
              <Box className="overview-sparkline" aria-hidden="true">
                {dailyEntries.slice(-10).map((item) => <span key={item.day} style={{ height: `${Math.max(8, (item.value / sparklineMax) * 100)}%` }} />)}
              </Box>
            </CardContent>
          </Card>
        ))}
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", lg: "2fr 1fr" },
          gap: 3,
        }}
      >
        <Card>
          <CardContent>
            <Typography variant="h6">Transaction value over time</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Daily recorded value · {data.currency} · UTC
            </Typography>
            {!Object.keys(data.daily).length ? (
              <Typography>No transactions in this period.</Typography>
            ) : (
              <React.Suspense fallback={<Skeleton height={256} />}>
                <FinancialChart daily={data.daily} currency={data.currency} />
              </React.Suspense>
            )}
            <Typography variant="caption" color="text.secondary">
              {Object.keys(data.daily)[0]} — {Object.keys(data.daily).at(-1)}
            </Typography>
            <details>
              <summary>View accessible chart data</summary>
              {Object.entries(data.daily).map(([day, v]) => (
                <Typography key={day}>
                  {day}: {money(v as number, data.currency)}
                </Typography>
              ))}
            </details>
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <Typography variant="h6">Status breakdown</Typography>
            {Object.entries(data.statuses).map(([k, v]) => (
              <Stack
                key={k}
                direction="row"
                justifyContent="space-between"
                sx={{
                  py: 1.5,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                }}
              >
                <Typography variant="body2">
                  {k.replaceAll("_", " ")}
                </Typography>
                <Chip size="small" label={String(v)} />
              </Stack>
            ))}
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <Typography variant="h6">Payment methods</Typography>
            {Object.entries(data.methods).map(([k, v]) => (
              <Box className="method-row" key={k}>
                <Box className="method-symbol">
                  <Icon name="Transactions" size={17} />
                </Box>
                <Box className="method-content">
                  <Box className="method-label">
                    <span>
                      {k === "UNASSIGNED"
                        ? "Unassigned"
                        : k === "PAYNOW"
                          ? "Paynow"
                          : k === "PAYPAL"
                            ? "PayPal"
                            : k}
                    </span>
                    <strong>{String(v)}</strong>
                  </Box>
                  <Box className="method-track">
                    <span
                      style={{
                        width:
                          (data.totalTx
                            ? (100 * Number(v)) / data.totalTx
                            : 0) + "%",
                      }}
                    />
                  </Box>
                </Box>
              </Box>
            ))}
          </CardContent>
        </Card>
      </Box>
    </>
  );
}
function Transactions({ params }: { params: any }) {
  const [page, setPage] = useState(0),
    [query, setQuery] = useState(""),
    [status, setStatus] = useState(""),
    [method, setMethod] = useState(""),
    [sort, setSort] = useState("createdAt"),
    [data, setData] = useState<any>(null),
    [error, setError] = useState(""),
    [detail, setDetail] = useState<any>(null),
    [busy, setBusy] = useState(false);
  useEffect(
    () => setPage(0),
    [query, status, method, sort, JSON.stringify(params)],
  );
  useEffect(() => {
    let active = true;
    setBusy(true);
    setError("");
    const timer = setTimeout(() => {
      api
        .get("/v1/admin/operations/transactions", {
          params: { ...params, page, size: 20, q: query, status, method, sort },
        })
        .then((r) => {
          if (active) setData(r.data.data);
        })
        .catch((e) => {
          if (active) setError(message(e));
        })
        .finally(() => {
          if (active) setBusy(false);
        });
    }, 250);
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [page, query, status, method, sort, JSON.stringify(params)]);
  async function open(id: string) {
    try {
      setDetail((await api.get(`/v1/admin/transactions/${id}`)).data.data);
    } catch (e) {
      setError(message(e));
    }
  }
  async function csv() {
    try {
      const response = await api.get("/v1/admin/operations/export", {
        params: { ...params, q: query, status, method },
        responseType: "blob",
      });
      const url = URL.createObjectURL(
          new Blob(["\uFEFF", response.data], {
            type: "text/csv;charset=utf-8",
          }),
        ),
        a = document.createElement("a");
      a.href = url;
      a.download = "securepay-transactions.csv";
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(
        "Export failed. Narrow the filters to at most 10,000 transactions and retry.",
      );
    }
  }

  return (
    <Card>
      <CardContent>
        <Stack direction="row" gap={2} flexWrap="wrap" sx={{ mb: 3 }}>
          <TextField
            label="Search ID or reference"
            size="small"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            inputProps={{ maxLength: 64 }}
          />
          <TextField
            select
            label="Status"
            size="small"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            sx={{ minWidth: 160 }}
          >
            {[
              "",
              "CREATED",
              "PAYPAL_ORDER_CREATED",
              "PAYPAL_CAPTURED",
              "PAYNOW_REQUESTED",
              "PAYNOW_PAID",
              "PAYNOW_FAILED",
              "PROOF_SUBMITTED",
              "NOTIFIED",
              "FAILED",
            ].map((s) => (
              <MenuItem key={s} value={s}>
                {s || "All statuses"}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Method"
            size="small"
            value={method}
            onChange={(e) => setMethod(e.target.value)}
            sx={{ minWidth: 130 }}
          >
            {["", "PAYPAL", "PAYNOW"].map((s) => (
              <MenuItem key={s} value={s}>
                {s || "All methods"}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Sort descending"
            size="small"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
          >
            {["createdAt", "amount", "status"].map((s) => (
              <MenuItem key={s} value={s}>
                {
                  (
                    {
                      createdAt: "Newest first",
                      amount: "Highest amount",
                      status: "Status",
                    } as Record<string, string>
                  )[s]
                }
              </MenuItem>
            ))}
          </TextField>
          <Button onClick={csv} disabled={!data?.content?.length || busy}>
            Export filtered CSV
          </Button>
        </Stack>
        {error && <Alert severity="error">{error}</Alert>}
        {busy ? (
          <Skeleton height={200} />
        ) : (
          <>
            <TransactionTable data={data?.content || []} onOpen={open} />

            {!data?.content?.length && (
              <Typography
                sx={{ p: 4, textAlign: "center" }}
                color="text.secondary"
              >
                No transactions match these filters.
              </Typography>
            )}
            <TablePagination
              component="div"
              count={data?.totalElements || 0}
              page={page}
              onPageChange={(_, n) => setPage(n)}
              rowsPerPage={20}
              rowsPerPageOptions={[20]}
            />
          </>
        )}
        <Dialog
          open={!!detail}
          onClose={() => setDetail(null)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>Transaction details</DialogTitle>
          <DialogContent>
            {detail && (
              <>
                <Typography variant="h4" sx={{ mb: 2 }}>
                  {money(
                    detail.transaction.amount,
                    detail.transaction.currency,
                  )}
                </Typography>
                {Object.entries(detail.transaction).map(([k, v]) => (
                  <Typography
                    key={k}
                    sx={{ overflowWrap: "anywhere", mb: 0.7 }}
                  >
                    <strong>{k}: </strong>
                    {String(v ?? "—")}
                  </Typography>
                ))}
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6">Receipt integrity record</Typography>
                {detail.proof ? (
                  ["proofHash", "stellarTxHash", "ledger", "createdAt"].map(
                    (key) => (
                      <Typography
                        key={key}
                        sx={{ overflowWrap: "anywhere", my: 1 }}
                      >
                        <strong>{key}: </strong>
                        {String(detail.proof[key] ?? "Not available")}
                      </Typography>
                    ),
                  )
                ) : (
                  <Typography color="text.secondary" sx={{ my: 1 }}>
                    No receipt integrity record is available.
                  </Typography>
                )}
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6">Status timeline</Typography>
                {detail.audit?.map((a: any) => (
                  <Typography key={a.id} sx={{ my: 1 }}>
                    {new Date(a.createdAt).toLocaleString()} · {a.newStatus} ·{" "}
                    {a.message}
                  </Typography>
                ))}
                {!detail.audit?.length && (
                  <Typography>No status events recorded.</Typography>
                )}
                <Alert severity="info" sx={{ mt: 2 }}>
                  Stored references and receipts alone do not establish provider
                  payment authenticity. Use Verification for an evidence review.
                </Alert>
              </>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDetail(null)}>Close</Button>
          </DialogActions>
        </Dialog>
      </CardContent>
    </Card>
  );
}
function Workflow({ page }: { page: string }) {
  const kind =
    page === "Verification"
      ? "VERIFICATION"
      : page === "Refunds"
        ? "REFUND"
        : page === "Reconciliation"
          ? "RECONCILIATION"
          : "RISK_REVIEW";
  const [id, setId] = useState(""),
    [reason, setReason] = useState(""),
    [csv, setCsv] = useState(""),
    [data, setData] = useState<any[]>([]),
    [flags, setFlags] = useState<any[]>([]),
    [error, setError] = useState(""),
    [success, setSuccess] = useState(""),
    [busy, setBusy] = useState(false),
    [confirm, setConfirm] = useState(false);
  async function load() {
    try {
      setData(
        (await api.get(`/v1/admin/operations/history/${kind}`)).data.data,
      );
      if (page === "Risk indicators")
        setFlags((await api.get("/v1/admin/operations/risk")).data.data);
    } catch (e) {
      setError(message(e));
    }
  }
  useEffect(() => {
    load();
  }, []);
  async function run() {
    setConfirm(false);
    setBusy(true);
    setError("");
    setSuccess("");
    try {
      if (page === "Reconciliation")
        await api.post("/v1/admin/operations/reconcile", { csv });
      else
        await api.post(
          `/v1/admin/operations/transactions/${encodeURIComponent(id)}/${page === "Verification" ? "verify" : page === "Refunds" ? "refund" : "risk-review"}`,
          { reason },
        );
      setSuccess(
        "Recorded successfully. Review the evidence and status below.",
      );
      await load();
    } catch (e) {
      setError(message(e));
    } finally {
      setBusy(false);
    }
  }
  const descriptions: Record<string, string> = {
    Verification:
      "Evidence review history. Without a fresh trusted provider response, results require manual review.",
    Refunds:
      "Create a full-amount manual refund request for a paid transaction. No funds move through this workflow; a provider refund must be handled externally.",
    Reconciliation:
      "Import settlement CSV using reference,amount,currency columns. Only unquoted fields are supported. Matching does not change balances or authenticate the imported evidence.",
    "Risk indicators":
      "Explainable rules identify investigation candidates, not proof of fraud. Record investigation notes against the transaction.",
  };
  return (
    <>
      <Alert severity="info" sx={{ mb: 3 }}>
        {descriptions[page]}
      </Alert>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>
            {page === "Reconciliation" ? "Import settlement" : "New review"}
          </Typography>
          <Stack gap={2}>
            {page === "Reconciliation" ? (
              <>
                <Button component="label" variant="outlined">
                  Choose settlement CSV
                  <input
                    type="file"
                    accept=".csv,text/csv"
                    hidden
                    onChange={async (e) => {
                      const file = e.target.files?.[0];
                      if (!file) return;
                      if (file.size > 200000) {
                        setError("CSV must be under 200 KB");
                        return;
                      }
                      setCsv(await file.text());
                    }}
                  />
                </Button>
                <TextField
                  multiline
                  minRows={5}
                  label="Settlement CSV"
                  value={csv}
                  onChange={(e) => setCsv(e.target.value)}
                  placeholder={
                    "reference,amount,currency\nPAYMENT-REFERENCE,63.50,USD"
                  }
                />
              </>
            ) : (
              <>
                <TextField
                  required
                  label="Transaction ID"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                />
                {page !== "Verification" && (
                  <TextField
                    required
                    multiline
                    minRows={2}
                    label="Reason / investigation note (10–500 characters)"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    inputProps={{ maxLength: 500 }}
                  />
                )}
              </>
            )}
            <Button
              variant="contained"
              disabled={
                busy ||
                (page === "Reconciliation"
                  ? !csv
                  : !id ||
                    (page !== "Verification" && reason.trim().length < 10))
              }
              onClick={() => setConfirm(true)}
            >
              {busy
                ? "Saving…"
                : page === "Reconciliation"
                  ? "Review settlement import"
                  : "Record " +
                    (page === "Refunds" ? "refund request" : "review")}
            </Button>
            {error && <Alert severity="error">{error}</Alert>}
            {success && <Alert severity="success">{success}</Alert>}
          </Stack>
        </CardContent>
      </Card>
      {flags.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6">Open rule indicators</Typography>
            {flags.map((t) => (
              <Box
                key={t.transactionId}
                sx={{
                  py: 2,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                }}
              >
                <Button onClick={() => setId(t.transactionId)}>
                  {t.transactionId}
                </Button>
                <Typography>
                  {money(t.amount, t.currency)} · {t.rules.join(" · ")}
                </Typography>
              </Box>
            ))}
          </CardContent>
        </Card>
      )}
      <Card>
        <CardContent>
          <Typography variant="h6">History · latest 100 records</Typography>
          {data.length === 0 ? (
            <Box className="empty-state">
              <Icon name={page} size={28} />
              <Typography>
                No reviews recorded. Use the form above to start a workflow.
              </Typography>
            </Box>
          ) : (
            data.map((o) => (
              <Box
                key={o.id}
                sx={{
                  py: 2,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                }}
              >
                <StatusBadge status={o.state} />
                <Typography sx={{ mt: 1, overflowWrap: "anywhere" }}>
                  {o.evidence}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {new Date(o.createdAt).toLocaleString()} ·{" "}
                  {o.transactionId || "Settlement row"} · actor {o.actor}
                </Typography>
              </Box>
            ))
          )}
        </CardContent>
      </Card>
      <Dialog open={confirm} onClose={() => setConfirm(false)}>
        <DialogTitle>Confirm {page.toLowerCase()}</DialogTitle>
        <DialogContent>
          <Typography>
            This records an audited operation.{" "}
            {page === "Refunds"
              ? "No funds will move. A provider refund must be processed externally."
              : page === "Reconciliation"
                ? "Imported rows will be matched against internal records. Balances remain unchanged."
                : "The evidence and result will appear in review history."}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm(false)}>Cancel</Button>
          <Button onClick={run}>Confirm and record</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
