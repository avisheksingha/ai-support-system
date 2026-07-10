import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useAuth } from "../hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, Eye, EyeOff, Lock, Mail } from "lucide-react";
import type { ApiError } from "@/lib/api-client";
import { Link } from "react-router-dom";

const loginSchema = z.object({
  email: z.string().email("Please enter a valid email address."),
  password: z.string().min(1, "Password is required."),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login } = useAuth();
  const [apiError, setApiError] = useState<{ message: string; type: "error" | "warning" } | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setApiError(null);
    try {
      await login(data);
    } catch (error) {
      const err = error as ApiError;
      if (err.status === 401) {
        const msg = err.message?.toLowerCase() ?? "";
        if (msg.includes("locked") || msg.includes("disabled")) {
          setApiError({
            message: "Your account has been locked. Please contact an administrator.",
            type: "warning",
          });
        } else {
          setApiError({
            message: "Invalid email or password. Please try again.",
            type: "error",
          });
        }
      } else {
        setApiError({
          message: err.message || "An unexpected error occurred during login.",
          type: "error",
        });
      }
    }
  };

  return (
    <div className="w-full flex flex-col gap-8">
      {/* Header */}
      <div className="space-y-1.5 text-center pb-2">
        <h2 className="text-xl font-semibold tracking-tight text-foreground">
          Log in to continue
        </h2>
        <p className="text-sm text-muted-foreground">
          to your AI Support account
        </p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

        {/* Email */}
        <div className="space-y-1.5">
          <Label htmlFor="email" className="text-sm font-medium text-foreground">
            Email address
          </Label>
          <div className="relative">
            <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              id="email"
              type="email"
              autoComplete="email"
              placeholder="you@aisupport.com"
              className="pl-10 h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:border-blue-500/50 transition-colors"
              {...register("email")}
            />
          </div>
          {errors.email && (
            <p className="text-xs font-medium text-red-400">{errors.email.message}</p>
          )}
        </div>

        {/* Password */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <Label htmlFor="password" className="text-sm font-medium text-foreground">
              Password
            </Label>
            <button
              type="button"
              className="text-xs text-blue-400 hover:text-blue-300 transition-colors font-medium"
              tabIndex={-1}
            >
              Forgot password?
            </button>
          </div>
          <div className="relative">
            <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              placeholder="••••••••"
              className="pl-10 pr-10 h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:border-blue-500/50 transition-colors"
              {...register("password")}
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              tabIndex={-1}
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          {errors.password && (
            <p className="text-xs font-medium text-red-400">{errors.password.message}</p>
          )}
        </div>

        {/* Error / Warning banner */}
        {apiError && (
          <div className={`flex items-start gap-3 p-3.5 rounded-lg text-sm border ${
            apiError.type === "warning"
              ? "bg-amber-500/8 border-amber-500/25 text-amber-300"
              : "bg-red-500/8 border-red-500/20 text-red-400"
          }`}>
            <span className="text-base leading-none mt-0.5 shrink-0">
              {apiError.type === "warning" ? "🔒" : "⚠️"}
            </span>
            <span>{apiError.message}</span>
          </div>
        )}

        {/* Submit */}
        <Button
          type="submit"
          className="w-full h-10 font-medium transition-colors border-0 mt-2"
          disabled={isSubmitting}
        >
          {isSubmitting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Authenticating…
            </>
          ) : (
            "Sign in"
          )}
        </Button>
      </form>

      {/* Divider */}
      <div className="flex items-center gap-3">
        <div className="flex-1 h-px bg-muted" />
        <span className="text-xs text-muted-foreground">Secure access only</span>
        <div className="flex-1 h-px bg-muted" />
      </div>

      {/* Help text */}
      <p className="text-center text-sm text-muted-foreground">
        Don't have an account?{" "}
        <Link to="/auth/signup" className="text-[#0C66E4] hover:text-[#0052CC] font-medium transition-colors">
          Sign up
        </Link>
      </p>
    </div>
  );
}
