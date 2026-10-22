import { apiClient } from "../../../services/apiClient";
import { PaymentRequest, Transaction } from "../types";

export async function createPaymentRequest(
  amount: number,
  currency: string,
  note: string | undefined,
  ttlMinutes: number,
  payerPhone: string | undefined,
  provider: string | undefined,
  paynowMethod: string | undefined,
  paynowPhone: string | undefined
) {
  const resp = await apiClient.post("/v1/payment-requests", {
    amount,
    currency,
    note,
    ttlMinutes,
    payerPhone,
    provider,
    paynowMethod,
    paynowPhone,
  });
  return resp.data.data as PaymentRequest;
}

function extractQrPayload(input: string) {
  const trimmed = input.trim();
  if (trimmed.startsWith("securepay://")) {
    const qrIndex = trimmed.indexOf("qr=");
    if (qrIndex >= 0) {
      const qrValue = trimmed.substring(qrIndex + 3);
      const nextParam = qrValue.indexOf("&");
      const encoded = nextParam >= 0 ? qrValue.substring(0, nextParam) : qrValue;
      return decodeURIComponent(encoded);
    }
  }
  return trimmed;
}

export async function consumePaymentRequest(qrInput: string) {
  const qrPayload = extractQrPayload(qrInput);
  const resp = await apiClient.post("/v1/payment-requests/consume", {
    qrPayload,
    returnUrl: "securepay://paypal-return",
    cancelUrl: "securepay://paypal-cancel",
  });
  return resp.data.data as Transaction;
}
