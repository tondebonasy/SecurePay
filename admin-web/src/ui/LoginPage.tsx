import { motion, useReducedMotion } from "motion/react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button as PremiumButton } from "../components/ui/button";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowUpRight,
  ChevronLeft,
  Eye,
  EyeOff,
  KeyRound,
  LoaderCircle,
  LockKeyhole,
  ShieldCheck,
  AlertCircle,
} from "lucide-react";
import { Input } from "../components/ui/input";
import { api } from "../util/api";
import { setAuth } from "../util/auth";

const schema = z.object({
  phone: z.string().trim().min(7, "Enter a valid phone number.").max(32),
  password: z.string().min(8, "Password must contain at least 8 characters."),
  email: z.string().refine((value) => !value || z.email().safeParse(value).success, "Enter a valid email address."),
  name: z.string().max(120),
  inviteCode: z.string().max(128),
});

type FormValues = z.infer<typeof schema>;
type LoginStage = "phone" | "password";

const GENERIC_SIGN_IN_ERROR = "Sign-in failed. Check your details and try again.";

export default function LoginPage() {
  const nav = useNavigate();
  const reducedMotion = useReducedMotion();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [stage, setStage] = useState<LoginStage>("phone");
  const [visible, setVisible] = useState(false);
  const [remember, setRemember] = useState(false);
  const [capsLock, setCapsLock] = useState(false);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [lockoutUntil, setLockoutUntil] = useState<number | null>(null);
  const [lockoutSeconds, setLockoutSeconds] = useState(0);
  const {
    watch,
    register,
    setError,
    clearErrors,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { phone: "", password: "", email: "", name: "", inviteCode: "" },
  });
  const values = watch();

  useEffect(() => {
    if (!lockoutUntil) return;
    const update = () => {
      const remaining = Math.max(0, Math.ceil((lockoutUntil - Date.now()) / 1000));
      setLockoutSeconds(remaining);
      if (!remaining) {
        setLockoutUntil(null);
        setFailedAttempts(0);
        setErr(null);
      }
    };
    update();
    const timer = window.setInterval(update, 1000);
    return () => window.clearInterval(timer);
  }, [lockoutUntil]);

  function switchMode(next: "login" | "register") {
    setMode(next);
    setStage("phone");
    setErr(null);
    setCapsLock(false);
    clearErrors();
  }

  function continueToPassword() {
    const result = schema.shape.phone.safeParse(values.phone);
    if (!result.success) {
      setError("phone", { type: "manual", message: "Enter a valid phone number." });
      return;
    }
    clearErrors("phone");
    setErr(null);
    setStage("password");
  }

  function returnToPhone() {
    setStage("phone");
    setVisible(false);
    setCapsLock(false);
    clearErrors("password");
    setErr(null);
  }

  async function submitLogin() {
    if (lockoutSeconds > 0) return;
    setErr(null);
    setBusy(true);
    try {
      const response = await api.post("/v1/auth/login", {
        phone: values.phone,
        password: values.password,
      });
      const data = response.data?.data;
      setAuth(
        { accessToken: data.accessToken, refreshToken: data.refreshToken, sessionId: data.sessionId },
        remember,
      );
      nav("/", { replace: true });
    } catch (error: any) {
      const isNetworkError = !error?.response;
      if (isNetworkError) {
        setErr("Cannot reach SecurePay. Check that the API is running and try again.");
      } else {
        const attempts = failedAttempts + 1;
        setFailedAttempts(attempts);
        if (attempts >= 4) {
          setLockoutUntil(Date.now() + 30_000);
          setErr("Too many attempts. Try again in 30 seconds.");
        } else {
          setErr(GENERIC_SIGN_IN_ERROR);
        }
      }
    } finally {
      setBusy(false);
    }
  }

  async function submitRegister() {
    setErr(null);
    setBusy(true);
    try {
      const response = await api.post("/v1/auth/register", {
        phone: values.phone,
        email: values.email,
        displayName: values.name,
        password: values.password,
      });
      const data = response.data?.data;
      setAuth({ accessToken: data.accessToken, refreshToken: data.refreshToken, sessionId: data.sessionId }, remember);
      if (values.inviteCode) await api.post("/v1/invites/redeem", { code: values.inviteCode });
      nav("/", { replace: true });
    } catch (error: any) {
      setErr(error?.response?.data?.error?.message || "Registration failed. Check your details and try again.");
    } finally {
      setBusy(false);
    }
  }

  const field = (
    key: keyof FormValues,
    label: string,
    type = "text",
    placeholder = "",
    autoComplete = "off",
  ) => (
    <div className="login-v2-field" key={key}>
      <label htmlFor={key}>{label}</label>
      <div className="login-v2-input-wrap">
        <Input
          id={key}
          type={key === "password" && visible ? "text" : type}
          placeholder={placeholder}
          autoComplete={autoComplete}
          required={key === "phone" || key === "password" || (key === "name" && mode === "register")}
          aria-invalid={!!errors[key]}
          aria-describedby={errors[key] ? `${key}-error` : undefined}
          className={`${key === "password" ? "pr-12" : ""} ${errors[key] ? "login-v2-input-error" : ""}`}
          onKeyDown={key === "password" ? (event) => setCapsLock(event.getModifierState("CapsLock")) : undefined}
          onKeyUp={key === "password" ? (event) => setCapsLock(event.getModifierState("CapsLock")) : undefined}
          {...register(key)}
        />
        {key === "password" && (
          <button type="button" onClick={() => setVisible((current) => !current)} aria-label={visible ? "Hide password" : "Show password"} className="login-v2-password-toggle">
            {visible ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
      {errors[key] && <p id={`${key}-error`} role="alert" className="login-v2-error">{errors[key]?.message}</p>}
    </div>
  );

  const loginForm = mode === "login" && stage === "phone" ? (
    <>
      {field("phone", "Phone number", "tel", "+263 77 000 0000", "tel")}
      <PremiumButton type="button" onClick={continueToPassword} className="login-v2-primary-button">
        Continue <ArrowUpRight size={17} />
      </PremiumButton>
    </>
  ) : mode === "login" ? (
    <>
      <div className="login-v2-identity-row">
        <span>{values.phone}</span>
        <button type="button" onClick={returnToPhone}>Change</button>
      </div>
      {field("password", "Password", "password", "Enter your password", "current-password")}
      {capsLock && <div className="login-v2-caps-warning" role="status"><AlertCircle size={15} /> Caps Lock is on</div>}
      <label className="login-v2-remember">
        <input type="checkbox" checked={remember} onChange={(event) => setRemember(event.target.checked)} />
        <span>Remember this device</span>
      </label>
      <PremiumButton type="submit" disabled={busy || lockoutSeconds > 0} className="login-v2-primary-button">
        {busy ? <LoaderCircle size={18} className="animate-spin" /> : lockoutSeconds > 0 ? `Try again in ${lockoutSeconds}s` : <>Sign in <ArrowUpRight size={17} /></>}
      </PremiumButton>
    </>
  ) : (
    <>
      {field("name", "Full name", "text", "Alex Morgan", "name")}
      {field("phone", "Phone number", "tel", "+263 77 000 0000", "tel")}
      {field("email", "Email (optional)", "email", "you@company.com", "email")}
      {field("password", "Password", "password", "Create a password", "new-password")}
      {field("inviteCode", "Administrator invite (optional)", "text", "Invitation code")}
      <label className="login-v2-remember">
        <input type="checkbox" checked={remember} onChange={(event) => setRemember(event.target.checked)} />
        <span>Remember this device</span>
      </label>
      <PremiumButton type="submit" disabled={busy} className="login-v2-primary-button">
        {busy ? <LoaderCircle size={18} className="animate-spin" /> : <>Create account <ArrowUpRight size={17} /></>}
      </PremiumButton>
    </>
  );

  return (
    <div className="login-v2 min-h-dvh text-slate-950 dark:text-white">
      <header className="login-v2-header">
        <a href="/login" className="login-v2-brand" aria-label="SecurePay home">
          <img src="/securepay.svg" alt="" width="34" height="34" />
          <span>SecurePay</span>
        </a>
      </header>

      <main className="login-v2-main">
        <section className="login-v2-story" aria-label="SecurePay overview">
          <p className="login-v2-story-kicker">SECURE PAYMENTS. BRIGHTER BUSINESS.</p>
          <h2>Payments<br />in <em>safe hands</em></h2>
          <p className="login-v2-story-copy">A secure and seamless way to manage payments, access your workspace, and keep your business moving with confidence.</p>
          <div className="login-v2-story-points">
            <div><ShieldCheck size={20} /><span><strong>Protected operations</strong><small>Your payment activity stays controlled.</small></span></div>
            <div><ArrowUpRight size={20} /><span><strong>Fast access</strong><small>Get to the work that needs attention.</small></span></div>
            <div><KeyRound size={20} /><span><strong>Built for business</strong><small>Clear tools for everyday payment work.</small></span></div>
          </div>
        </section>
        <motion.section initial={reducedMotion ? false : { opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.28, ease: "easeOut" }} className="login-v2-form" aria-labelledby="auth-title">
          <div className="login-v2-form-mark">{mode === "login" ? <LockKeyhole size={20} strokeWidth={1.8} /> : <KeyRound size={20} strokeWidth={1.8} />}</div>
          <p className="login-v2-kicker">{mode === "login" ? "Workspace access" : "Account setup"}</p>
          <h1 id="auth-title">{mode === "login" ? stage === "phone" ? "Sign in to your workspace" : "Enter your password" : "Create your workspace"}</h1>
          <p className="login-v2-subtitle">{mode === "login" ? stage === "phone" ? "Use the phone number associated with your account." : "Enter the password for this account to continue." : "Create the first administrator account for this workspace."}</p>

          <form noValidate onSubmit={handleSubmit(mode === "login" ? submitLogin : submitRegister)} className="login-v2-form-fields">
            {err && <div role="alert" className="login-v2-alert"><AlertCircle size={16} /><span>{err}</span></div>}
            {loginForm}
          </form>

          <div className="login-v2-switch">
            <span>{mode === "login" ? "New to SecurePay?" : "Already have an account?"}</span>
            <button type="button" onClick={() => switchMode(mode === "login" ? "register" : "login")}>
              {mode === "login" ? "Create an account" : "Sign in instead"}
            </button>
          </div>
          <div className="login-v2-footnote"><ShieldCheck size={14} /> Secured sign-in</div>
        </motion.section>

      </main>

      <footer className="login-v2-footer"><span>© {new Date().getFullYear()} SecurePay</span></footer>
    </div>
  );
}
