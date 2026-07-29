export function normalizeTicketSubject(subject?: string): string {
  if (!subject) return "General Support Inquiry";
  const trimmed = subject.trim();
  const lower = trimmed.toLowerCase();
  
  // Normalize the varying versions of the personal data export ticket
  if (lower === "requesting a copy of my personal data" || lower === "request for a copy of my personal data") {
    return "Request for a Copy of My Personal Data";
  }
  
  return trimmed;
}

export function formatIntent(intent?: string): string {
  if (!intent) return "General Support Inquiry";
  
  // Handle specific known cases
  const lower = intent.toLowerCase();
  if (lower === "gdpr" || lower === "gdpr_data_export") return "GDPR Data Privacy";
  if (lower === "iam" || lower === "account_access") return "Account Access";
  
  // Convert SNAKE_CASE or kebab-case to Title Case
  const clean = intent.replace(/[-_]/g, ' ');
  return clean
    .split(/\s+/)
    .map(word => {
      // Keep acronyms uppercase
      const upperWord = word.toUpperCase();
      if (['API', 'UI', 'UX', 'GDPR', 'IAM', 'SSO', 'L1', 'L2'].includes(upperWord)) {
        return upperWord;
      }
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    })
    .join(' ');
}

export function formatTeamName(team?: string): string {
  if (!team || team.toLowerCase() === "unassigned") return "Unassigned";
  
  const lower = team.toLowerCase();
  
  // Handle specific known teams
  if (lower === "compliance-team" || lower === "compliance_team") return "Compliance Team";
  if (lower === "l1_support" || lower === "l1-support") return "L1 Support Team";
  if (lower === "l2_support" || lower === "l2-support") return "L2 Support Team";
  if (lower === "tier2-technical" || lower === "tier2_technical") return "Tier 2 Technical Support";
  if (lower === "billing-support" || lower === "billing_support") return "Billing Support Team";
  if (lower === "general-support" || lower === "general_support") return "General Support Team";
  if (lower === "security-incident" || lower === "security_incident") return "Security Incident Response Team";
  
  // Generic fallback
  const clean = team.replace(/[-_]/g, ' ');
  const titleCased = clean
    .split(/\s+/)
    .map(word => {
      const upperWord = word.toUpperCase();
      if (['L1', 'L2', 'API'].includes(upperWord)) return upperWord;
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    })
    .join(' ');
    
  return titleCased.endsWith("Team") || titleCased.endsWith("Support") ? titleCased : `${titleCased} Team`;
}
