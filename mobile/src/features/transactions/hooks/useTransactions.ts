import { useCallback, useEffect, useState } from "react";
import { useFocusEffect } from "@react-navigation/native";
import * as txApi from "../api/transactionApi";
import { Transaction } from "../types";
import { getTransactionCache, setTransactionCache } from "../store/transactionSlice";
import { subscribeTransactionUpdates } from "../store/transactionEvents";

export function useTransactions() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [items, setItems] = useState<Transaction[]>([]);

  const load = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const cached = getTransactionCache();
      if (cached) setItems(cached.items);
      const list = await txApi.listTransactions();
      setItems(list);
      setTransactionCache(list);
    } catch (e: any) {
      setError(e?.response?.data?.error?.message || "Failed to load transactions");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    return subscribeTransactionUpdates(() => {
      load();
    });
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  return { loading, error, items, refresh: load };
}
