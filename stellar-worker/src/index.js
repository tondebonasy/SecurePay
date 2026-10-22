import "dotenv/config";
import express from "express";
import * as StellarSdk from "stellar-sdk";

const app = express();
app.use(express.json({ limit: "1mb" }));

const enabled = (process.env.STELLAR_ENABLED || "false").toLowerCase() === "true";
const horizonUrl = process.env.STELLAR_HORIZON_URL || "https://horizon-testnet.stellar.org";
const networkPassphrase = process.env.STELLAR_NETWORK_PASSPHRASE || "Test SDF Network ; September 2015";
const secret = process.env.STELLAR_SECRET_KEY || "";

const server = new StellarSdk.Horizon.Server(horizonUrl);

function requireEnabled() {
  if (!enabled) {
    const err = new Error("Stellar is disabled");
    err.status = 400;
    throw err;
  }
  if (!secret) {
    const err = new Error("STELLAR_SECRET_KEY not configured");
    err.status = 400;
    throw err;
  }
}

app.get("/health", (_req, res) => res.json({ ok: true, enabled, horizonUrl }));

app.post("/anchor", async (req, res, next) => {
  try {
    requireEnabled();
    const { txId, receiptHash } = req.body || {};
    if (!txId || !receiptHash) return res.status(400).json({ error: "txId and receiptHash required" });
    if (!/^[0-9a-fA-F]{64}$/.test(receiptHash)) {
      return res.status(400).json({ error: "receiptHash must be 64 hex chars (sha256)" });
    }


    const kp = StellarSdk.Keypair.fromSecret(secret);
    const account = await server.loadAccount(kp.publicKey());

    // Store the raw 32-byte hash as manageData value (no memo).
    const op = StellarSdk.Operation.manageData({
      name: `mg:${txId}`,
      value: Buffer.from(receiptHash, "hex"),
    });

    const tx = new StellarSdk.TransactionBuilder(account, {
      fee: StellarSdk.BASE_FEE,
      networkPassphrase,
    })
      .addOperation(op)
      .setTimeout(30)
      .build();

    tx.sign(kp);

    const submit = await server.submitTransaction(tx);
    return res.json({ stellarTxHash: submit.hash, ledger: submit.ledger });
  } catch (e) {
    next(e);
  }
});

app.get("/verify/:hash", async (req, res, next) => {
  try {
    requireEnabled();
    const { hash } = req.params;
    const receiptHash = req.query.receiptHash;
    if (!hash) return res.status(400).json({ verified: false, reason: "hash required" });
    if (!receiptHash) return res.status(400).json({ verified: false, reason: "receiptHash required" });
    if (!/^[0-9a-fA-F]{64}$/.test(receiptHash)) {
      return res.status(400).json({ verified: false, reason: "receiptHash must be 64 hex chars" });
    }

    // Check tx exists
    const tx = await server.transactions().transaction(hash);

    // Check any manage_data operation contains the receipt hash
    const ops = await server.operations().forTransaction(hash).call();
    const records = ops.records || [];
    const expected = Buffer.from(receiptHash, "hex");
    for (const r of records) {
      if (r.type === "manage_data") {
        // Horizon returns value as base64 string in some versions. Try both.
        const v = r.value;
        if (typeof v === "string") {
          try {
            const decoded = Buffer.from(v, "base64");
            if (decoded.equals(expected)) return res.json({ verified: true, reason: "OK" });
            if (decoded.toString("utf-8") === receiptHash) return res.json({ verified: true, reason: "OK" });
          } catch {}
        }
      }
    }
    return res.json({ verified: false, reason: "No matching manage_data value" });
  } catch (e) {
    if (e.response && e.response.status === 404) return res.json({ verified: false, reason: "Not found" });
    next(e);
  }
});

app.use((err, _req, res, _next) => {
  const status = err.status || 500;
  res.status(status).json({ error: err.message || "Error" });
});

const port = process.env.PORT || 3030;
app.listen(port, process.env.HOST || '127.0.0.1', () => console.log(`stellar-worker listening on ${port}, enabled=${enabled}`));
