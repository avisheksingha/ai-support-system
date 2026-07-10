import { apiClient } from "@/lib/api-client";

export interface WritingImproveRequest {
  context: "SUPPORT_TICKET" | "AGENT_REPLY" | "KNOWLEDGE_ARTICLE" | "INTERNAL_COMMENT" | "EMAIL";
  subject: string;
  content: string;
  language?: string;
}

export interface WritingImproveResponse {
  improvedSubject: string;
  improvedContent: string;
  changes: string[];
  improved: boolean;
  model: string;
}

export const writingApi = {
  improve: async (data: WritingImproveRequest): Promise<WritingImproveResponse> => {
    const response = await apiClient.post<WritingImproveResponse>("/analysis/writing/improve", data);
    return response.data;
  }
};
