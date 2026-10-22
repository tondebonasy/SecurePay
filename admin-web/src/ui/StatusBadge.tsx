import React from "react";
export function StatusBadge({ status }: { status: string }) {
  const good =
    /PAID|CAPTURED|NOTIFIED|MATCHED|VERIFIED|ACTIVE|REVIEWED/.test(status) &&
    !/UNVERIFIED|MISMATCH|INACTIVE/.test(status);
  const bad = /FAILED|MISMATCH|DUPLICATE|DISABLED|MISSING/.test(status);
  const label = status
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/^./, (c) => c.toUpperCase());
  return (
    <span
      className={
        "status-badge " + (bad ? "error" : good ? "success" : "pending")
      }
    >
      {label}
    </span>
  );
}
