export type Proof = {
  proofHash: string;
  stellarTxHash: string | null;
  ledger?: number;
  createdAt?: string;
};

export type ProofVerification = {
  verified: boolean;
  reason?: string;
};
