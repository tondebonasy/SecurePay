import React from "react";
import {
  LayoutDashboard,
  ArrowLeftRight,
  ShieldCheck,
  ScanLine,
  Undo2,
  TriangleAlert,
  Users,
  BriefcaseBusiness,
  FileChartColumn,
  Settings,
  ArrowRight,
  Moon,
  Sun,
  LockKeyhole,
  Eye,
  Menu,
  X,
  LogOut,
  ChevronDown,
  Search,
  Bell,
  CircleUserRound,
  type LucideIcon,
} from "lucide-react";
const icons: Record<string, LucideIcon> = {
  Overview: LayoutDashboard,
  Transactions: ArrowLeftRight,
  Verification: ShieldCheck,
  Reconciliation: ScanLine,
  Refunds: Undo2,
  "Risk indicators": TriangleAlert,
  Customers: Users,
  Administration: BriefcaseBusiness,
  Reports: FileChartColumn,
  Settings,
  arrow: ArrowRight,
  moon: Moon,
  sun: Sun,
  lock: LockKeyhole,
  eye: Eye,
  menu: Menu,
  close: X,
  logout: LogOut,
  chevron: ChevronDown,
  search: Search,
  bell: Bell,
  profile: CircleUserRound,
};
export function Icon({ name, size = 20 }: { name: string; size?: number }) {
  const Component = icons[name] || ShieldCheck;
  return (
    <Component
      size={size}
      strokeWidth={1.65}
      aria-hidden="true"
      style={{ flexShrink: 0 }}
    />
  );
}
