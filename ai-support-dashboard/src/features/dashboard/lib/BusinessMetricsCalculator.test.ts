import { describe, it, expect } from 'vitest';
import { BusinessMetricsCalculator } from './BusinessMetricsCalculator';
import type { TicketModel } from '@/shared/types/ticket';

describe('BusinessMetricsCalculator', () => {
  const mockTickets: TicketModel[] = [
    { id: 1, ticketNumber: "T-001", customerUserId: 101, customerEmail: "a@b.com", customerName: "A", subject: "S", message: "M", status: "NEW", priority: "HIGH", createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
    { id: 2, ticketNumber: "T-002", customerUserId: 102, customerEmail: "a@b.com", customerName: "A", subject: "S", message: "M", status: "RESOLVED", priority: "MEDIUM", createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
    { id: 3, ticketNumber: "T-003", customerUserId: 103, customerEmail: "a@b.com", customerName: "A", subject: "S", message: "M", status: "IN_PROGRESS", priority: "HIGH", createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
  ];

  it('calculates open tickets correctly', () => {
    const openCount = BusinessMetricsCalculator.calculateOpenTickets(mockTickets);
    expect(openCount).toBe(2);
  });

  it('calculates resolved today correctly', () => {
    const resolvedCount = BusinessMetricsCalculator.calculateResolvedToday(mockTickets);
    expect(resolvedCount).toBe(1);
  });
});
