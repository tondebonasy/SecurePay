import React, { useMemo } from "react";
import { tableFeatures, useTable, type ColumnDef } from "@tanstack/react-table";
import { Button } from "./ui/button";
import { StatusBadge } from "../ui/StatusBadge";
type TransactionRow = { id: string; amount: number; currency: string; status: string; createdAt: string; paynowReference?: string; paypalCaptureId?: string };
const features = tableFeatures({});
export function TransactionTable({
  data,
  onOpen,
}: {
  data: TransactionRow[];
  onOpen: (id: string) => void;
}) {
  const columns = useMemo<ColumnDef<typeof features, TransactionRow>[]>(
    () => [
      {
        accessorKey: "id",
        header: "Transaction",
        cell: ({ row }) => (
          <div>
            <span className="font-mono text-xs">
              {row.original.id.slice(0, 8)}
            </span>
            <span className="mt-1 block text-xs text-slate-500">
              {row.original.paynowReference ||
                row.original.paypalCaptureId ||
                "No provider reference"}
            </span>
          </div>
        ),
      },
      {
        accessorKey: "amount",
        header: "Amount",
        cell: ({ row }) =>
          new Intl.NumberFormat(undefined, {
            style: "currency",
            currency: row.original.currency,
          }).format(row.original.amount),
      },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ row }) => <StatusBadge status={row.original.status} />,
      },
      {
        accessorKey: "createdAt",
        header: "Created",
        cell: ({ row }) => new Date(row.original.createdAt).toLocaleString(),
      },
      {
        id: "actions",
        header: "Action",
        cell: ({ row }) => (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onOpen(row.original.id)}
          >
            View details
          </Button>
        ),
      },
    ],
    [onOpen],
  );
  const table = useTable({
    features,
    data,
    columns,
    getRowId: (row) => row.id,
  });
  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200/60 dark:border-slate-700">
      <table aria-label="Transactions" className="w-full border-collapse text-left text-sm">
        <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500 dark:bg-slate-900">
          {table.getHeaderGroups().map((group) => (
            <tr key={group.id}>
              {group.headers.map((header) => (
                <th
                  key={header.id}
                  scope="col"
                  className="px-5 py-4 font-semibold"
                >
                  <table.FlexRender header={header} />
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody className="divide-y divide-slate-200/60 dark:divide-slate-700">
          {table.getRowModel().rows.map((row) => (
            <tr
              key={row.id}
              className="transition-colors hover:bg-blue-50/40 dark:hover:bg-slate-800"
            >
              {row.getAllCells().map((cell) => (
                <td key={cell.id} className="whitespace-nowrap px-5 py-4">
                  <table.FlexRender cell={cell} />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}


