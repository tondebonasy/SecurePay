import * as React from "react";
import { cn } from "../../lib/utils";
export const Input = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, type, ...props }, ref) => (
  <input
    type={type}
    ref={ref}
    className={cn(
      "box-border flex h-12 font-sans w-full rounded-xl border border-solid border-slate-200 bg-slate-50/70 px-3.5 text-base sm:text-sm text-slate-900 shadow-none outline-none transition placeholder:text-slate-400 hover:border-slate-300 focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-900 dark:text-white dark:focus:bg-slate-900",
      className,
    )}
    {...props}
  />
));
Input.displayName = "Input";

