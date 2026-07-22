export interface StatusMapping {
  label: string;
  style: string;
  progress: {
    isReviewed: boolean;
    isAssigned: boolean;
    isResolved: boolean;
  };
}

export function getCustomerStatusMapping(status: string): StatusMapping {
  let style = "text-muted-foreground bg-muted border-border";
  let label = status;
  let isReviewed = false;
  let isAssigned = false;
  let isResolved = false;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-600 bg-blue-50 border-blue-200";
      label = "Submitted";
      break;
    case "ASSIGNED":
      style = "text-purple-600 bg-purple-50 border-purple-200";
      label = "In Review";
      isReviewed = true;
      isAssigned = true;
      break;
    case "IN_PROGRESS":
      style = "text-orange-600 bg-orange-50 border-orange-200";
      label = "In Progress";
      isReviewed = true;
      isAssigned = true;
      break;
    case "RESOLVED":
      style = "text-emerald-600 bg-emerald-50 border-emerald-200";
      label = "Resolved";
      isReviewed = true;
      isAssigned = true;
      isResolved = true;
      break;
    case "CLOSED":
      style = "text-gray-600 bg-gray-50 border-gray-200";
      label = "Closed";
      isReviewed = true;
      isAssigned = true;
      isResolved = true;
      break;
  }

  return {
    label,
    style,
    progress: {
      isReviewed,
      isAssigned,
      isResolved
    }
  };
}
