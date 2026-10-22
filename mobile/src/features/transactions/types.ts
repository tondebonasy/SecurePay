export type TxStatus =
  | "CREATED"
  | "PAYPAL_ORDER_CREATED"
  | "PAYPAL_CAPTURED"
  | "PAYNOW_REQUESTED"
  | "PAYNOW_PAID"
  | "PAYNOW_FAILED"
  | "PROOF_SUBMITTED"
  | "NOTIFIED"
  | "FAILED";

export type Transaction = {
  id: string;
  userId: string;
  paidByUserId?: string;
  payeeUserId?: string;
  paidByPhone?: string;
  paidByName?: string;
  payeePhone?: string;
  payeeName?: string;
  amount: number;
  currency: string;
  status: TxStatus;
  paypalOrderId?: string;
  paypalCaptureId?: string;
  paynowReference?: string;
  paynowMethod?: string;
  paynowPhone?: string;
  approveUrl?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type PaymentRequest = {
  id: string;
  payeeId: string;
  amount: number;
  currency: string;
  note?: string;
  provider?: string;
  paynowMethod?: string;
  paynowPhone?: string;
  status: "ACTIVE" | "CONSUMED" | "CANCELLED" | "EXPIRED";
  expiresAt?: string;
  createdAt?: string;
  qrPayload: string;
  token: string;
};
