import { apiClient } from "../../../services/apiClient";
import { Proof, ProofVerification } from "../types";

export async function getProof(txId: string) {
  const resp = await apiClient.get(`/v1/transactions/${txId}/proof`);
  return resp.data.data as Proof;
}

export async function verifyProof(txId: string) {
  const resp = await apiClient.get(`/v1/transactions/${txId}/verify-proof`);
  return resp.data.data as ProofVerification;
}
