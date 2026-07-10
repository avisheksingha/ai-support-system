import { format, formatDistanceToNow } from "date-fns";

export function parseDate(value: any): Date {
  if (!value) return new Date();
  if (value instanceof Date) return value;
  
  // Spring Boot Jackson array format: [year, month, day, hour, minute, second, nano]
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
    return new Date(Date.UTC(year, month - 1, day, hour, minute, second, nano / 1000000));
  }
  
  if (typeof value === 'number') {
    // Treat as seconds if smaller than year 2286
    if (value < 10000000000) return new Date(value * 1000);
    return new Date(value);
  }
  
  if (typeof value === 'string') {
    // Treat naive ISO strings from backend as UTC
    if (value.includes('T') && !value.endsWith('Z') && !value.match(/[+-]\d{2}:?\d{2}$/)) {
      value = value + 'Z';
    }
  }

  const parsed = new Date(value);
  if (isNaN(parsed.getTime()) && typeof value === 'string') {
    const parsedFloat = parseFloat(value);
    if (!isNaN(parsedFloat)) {
      if (parsedFloat < 10000000000) return new Date(parsedFloat * 1000);
      return new Date(parsedFloat);
    }
  }
  
  return parsed;
}

export function formatDateStr(date: any): string {
  if (!date) return "—";
  return parseDate(date).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

export function formatLongDateStr(date: any): string {
  if (!date) return "—";
  return parseDate(date).toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" });
}

export function formatTimeAgo(date: any): string {
  if (!date) return "";
  return formatDistanceToNow(parseDate(date), { addSuffix: true });
}

export function formatTime(date: any): string {
  if (!date) return "";
  return format(parseDate(date), "MMM d, h:mm a");
}
