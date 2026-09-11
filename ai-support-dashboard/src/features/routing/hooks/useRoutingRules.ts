import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { routingRulesApi } from "../api/routingRulesApi";
import type { RoutingRuleRequest, RoutingRuleFilterParams } from "../types";

export const ROUTING_RULES_KEY = ["routing-rules"] as const;
export const ROUTING_STATS_KEY = ["routing-rules", "stats"] as const;

export function useRoutingRulesQuery(params?: RoutingRuleFilterParams) {
  return useQuery({
    queryKey: [...ROUTING_RULES_KEY, params],
    queryFn: () => routingRulesApi.getRules(params),
  });
}

export function useRoutingRuleStatsQuery() {
  return useQuery({
    queryKey: ROUTING_STATS_KEY,
    queryFn: () => routingRulesApi.getStats(),
  });
}

export function useRoutingRuleQuery(id?: number) {
  return useQuery({
    queryKey: [...ROUTING_RULES_KEY, id],
    queryFn: () => (id ? routingRulesApi.getRule(id) : null),
    enabled: !!id,
  });
}

export function useCreateRoutingRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: RoutingRuleRequest) => routingRulesApi.createRule(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ROUTING_RULES_KEY });
    },
  });
}

export function useUpdateRoutingRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: RoutingRuleRequest }) =>
      routingRulesApi.updateRule(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ROUTING_RULES_KEY });
    },
  });
}

export function useToggleRoutingRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => routingRulesApi.toggleRuleActive(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ROUTING_RULES_KEY });
    },
  });
}

export function useDeleteRoutingRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => routingRulesApi.deleteRule(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ROUTING_RULES_KEY });
    },
  });
}
