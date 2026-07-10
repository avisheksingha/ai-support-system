import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useAuth } from "../hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, Eye, EyeOff, Lock, Mail, User } from "lucide-react";
import type { ApiError } from "@/lib/api-client";
import { Link } from "react-router-dom";

const signupSchema = z.object({
  fullName: z.string().min(2, "Full name must be at least 2 characters."),
  email: z.string().email("Please enter a valid email address."),
  password: z.string().min(6, "Password must be at least 6 characters."),
});

type SignupFormValues = z.infer<typeof signupSchema>;

export function SignupPage() {
  const { register: registerUser } = useAuth();
  const [apiError, setApiError] = useState<{ message: string; type: "error" | "warning" } | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: { fullName: "", email: "", password: "" },
  });

  const onSubmit = async (data: SignupFormValues) => {
    setApiError(null);
    try {
      await registerUser(data);
    } catch (error) {
      const err = error as ApiError;
      setApiError({
        message: err.message || "An unexpected error occurred during signup.",
        type: "error",
      });
    }
  };

  return (
    <div className="w-full flex flex-col gap-8">
      {/* Header */}
      <div className="space-y-1.5 text-center pb-2">
        <h2 className="text-xl font-semibold tracking-tight text-foreground">
          Create an account
        </h2>
        <p className="text-sm text-muted-foreground">
          Join the AI Support Ops platform
        </p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        
        {/* Full Name */}
        <div className="space-y-1.5">
          <Label htmlFor="fullName" className="text-sm font-medium text-foreground">
            Full name
          </Label>
          <div className="relative">
            <User className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              id="fullName"
              type="text"
              autoComplete="name"
              placeholder="John Doe"
              className="pl-10 h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-[#0C66E4] focus-visible:border-[#0C66E4]/50 transition-colors"
              {...register("fullName")}
            />
          </div>
          {errors.fullName && (
            <p className="text-xs font-medium text-red-400">{errors.fullName.message}</p>
          )}
        </div>

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
              className="pl-10 h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-[#0C66E4] focus-visible:border-[#0C66E4]/50 transition-colors"
              {...register("email")}
            />
          </div>
          {errors.email && (
            <p className="text-xs font-medium text-red-400">{errors.email.message}</p>
          )}
        </div>

        {/* Password */}
        <div className="space-y-1.5">
          <Label htmlFor="password" className="text-sm font-medium text-foreground">
            Password
          </Label>
          <div className="relative">
            <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="new-password"
              placeholder="••••••••"
              className="pl-10 pr-10 h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-[#0C66E4] focus-visible:border-[#0C66E4]/50 transition-colors"
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

        {/* Error banner */}
        {apiError && (
          <div className={`flex items-start gap-3 p-3.5 rounded-lg text-sm border bg-red-500/8 border-red-500/20 text-red-400`}>
            <span className="text-base leading-none mt-0.5 shrink-0">⚠️</span>
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
              Creating account…
            </>
          ) : (
            "Sign up"
          )}
        </Button>
      </form>

      {/* Help text */}
      <p className="text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link to="/auth/login" className="text-[#0C66E4] hover:text-[#0052CC] font-medium transition-colors">
          Log in
        </Link>
      </p>
    </div>
  );
}
