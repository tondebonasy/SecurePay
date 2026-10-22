export type RootStackParamList = {
  MainTabs: undefined;
  PaymentStatus: { txId: string };
  TransactionDetail: { txId: string };
  ProofViewer: { txId: string };
};

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
};
