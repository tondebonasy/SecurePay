import { apiClient } from "../../../services/apiClient";
import { Transaction } from "../types";

export async function initiate(amount: number, currency: string, description?: string) {
  const resp = await apiClient.post("/v1/transactions/initiate", {
    amount,
    currency,
    description,
    returnUrl: "securepay://paypal-return",
    cancelUrl: "securepay://paypal-cancel",
  }, { headers: { "Idempotency-Key": `tx-${Date.now()}` } });
  return resp.data.data as Transaction;
}

export async function getTransaction(id: string) {
  const resp = await apiClient.get(`/v1/transactions/${id}`);
  return resp.data.data as Transaction;
}

export async function listTransactions() {
  const resp = await apiClient.get("/v1/transactions");
  return resp.data.data as Transaction[];
}

export async function confirm(id: string) {
  const resp = await apiClient.post(`/v1/transactions/${id}/confirm`, {});
  return resp.data.data as Transaction;
}

export async function paynowRemote(
  amount: number,
  currency: string,
  description: string | undefined,
  phone: string,
  method: string,
  payeePhone: string
) {
  const resp = await apiClient.post("/v1/paynow/remote", {
    amount,
    currency,
    description,
    phone,
    method,
    payeePhone,
  });
  return resp.data.data as Transaction;
}

export async function paynowPoll(id: string) {
  const resp = await apiClient.post(`/v1/paynow/${id}/poll`, {});
  return resp.data.data as Transaction;
}

export async function paynowTrace(id: string) {
  const resp = await apiClient.post(`/v1/paynow/${id}/trace`, {});
  return resp.data.data as Transaction;
}
