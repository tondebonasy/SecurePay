import { Transaction } from "../types";

type Cache = { items: Transaction[]; updatedAt: number };
let cache: Cache | null = null;

export function getTransactionCache() {
  return cache;
}

export function setTransactionCache(items: Transaction[]) {
  cache = { items, updatedAt: Date.now() };
}

export function clearTransactionCache() {
  cache = null;
}
