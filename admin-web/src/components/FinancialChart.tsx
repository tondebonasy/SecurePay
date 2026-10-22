import React from "react";
import {
  AreaChart,
  Area,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
export function FinancialChart({
  daily,
  currency,
}: {
  daily: Record<string, number>;
  currency: string;
}) {
  const data = Object.entries(daily)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, value]) => ({ date, value: Number(value) }));
  const format = (v: number) =>
    new Intl.NumberFormat(undefined, {
      style: "currency",
      currency,
      maximumFractionDigits: 0,
    }).format(v);
  return (
    <div
      className="h-64 w-full min-w-0"
      role="img"
      aria-label="Daily transaction value chart"
    >
      <ResponsiveContainer width="100%" height="100%" minWidth={0}>
        <AreaChart
          data={data}
          margin={{ top: 12, right: 12, bottom: 8, left: 12 }}
          accessibilityLayer
        >
          <defs>
            <linearGradient id="paymentValue" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#2563eb" stopOpacity={0.25} />
              <stop offset="100%" stopColor="#2563eb" stopOpacity={0.02} />
            </linearGradient>
          </defs>
          <CartesianGrid
            strokeDasharray="3 5"
            vertical={false}
            stroke="#94a3b833"
          />
          <XAxis
            dataKey="date"
            tickFormatter={(v) => v.slice(5)}
            tick={{ fontSize: 11, fill: "#94a3b8" }}
            axisLine={false}
            tickLine={false}
            minTickGap={24}
          />
          <YAxis
            tickFormatter={format}
            tick={{ fontSize: 10, fill: "#94a3b8" }}
            axisLine={false}
            tickLine={false}
            width={78}
          />
          <Tooltip
            formatter={(v) => format(Number(v))}
            contentStyle={{
              borderRadius: 12,
              border: "1px solid #cbd5e1",
              color: "#0f172a",
            }}
          />
          <Area
            type="monotone"
            dataKey="value"
            name="Transaction value"
            stroke="#2563eb"
            fill="url(#paymentValue)"
            strokeWidth={2.5}
            isAnimationActive={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
