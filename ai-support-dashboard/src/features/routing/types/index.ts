import type { TicketPriority } from "@/shared/types/ticket";

export interface RoutingRule {
  id: number;
  ruleVersion: number;
  ruleName: string;
  description?: string | undefined;
  priority: number;
  active: boolean;
  intentPattern?: string | undefined;
  sentimentPattern?: string | undefined;
  urgencyPattern?: string | undefined;
  keywordPatterns: string[];
  assignToTeam: string;
  priorityOverride?: TicketPriority | undefined;
  slaHours?: number | undefined;
  createdBy?: string | undefined;
  updatedBy?: string | undefined;
  createdAt: string;
  updatedAt: string;
}

export interface RoutingRuleRequest {
  ruleName: string;
  description?: string | undefined;
  priority: number;
  active: boolean;
  intentPattern?: string | undefined;
  sentimentPattern?: string | undefined;
  urgencyPattern?: string | undefined;
  keywordPatterns?: string[] | undefined;
  assignToTeam: string;
  priorityOverride?: TicketPriority | undefined;
  slaHours?: number | undefined;
}

export interface RoutingRuleStats {
  totalRules: number;
  activeRules: number;
  inactiveRules: number;
  totalTeamsCount: number;
  targetTeams: string[];
}

export interface RoutingRuleFilterParams {
  page?: number | undefined;
  size?: number | undefined;
  search?: string | undefined;
  active?: boolean | undefined;
  team?: string | undefined;
}

export interface Page<T> {
  content: T[];
  totalElements?: number | undefined;
  totalPages?: number | undefined;
  size?: number | undefined;
  number?: number | undefined;
  page?: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  } | undefined;
}
