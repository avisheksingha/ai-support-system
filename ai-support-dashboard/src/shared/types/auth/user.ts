import type { Role } from "./role";

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  enabled: boolean;
  locked: boolean;
  createdAt?: string;
}
