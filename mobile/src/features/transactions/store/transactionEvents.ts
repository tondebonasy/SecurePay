const listeners = new Set<() => void>();

export function subscribeTransactionUpdates(listener: () => void) {
  listeners.add(listener);
  return () => { listeners.delete(listener); };
}

export function notifyTransactionUpdated() {
  for (const listener of listeners) {
    listener();
  }
}
