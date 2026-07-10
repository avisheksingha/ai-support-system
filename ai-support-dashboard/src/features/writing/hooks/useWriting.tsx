import { useMutation } from "@tanstack/react-query";
import { writingApi } from "../api/writingApi";
import type { WritingImproveRequest, WritingImproveResponse } from "../api/writingApi";

export const useImproveWriting = () => {
  return useMutation<WritingImproveResponse, Error, WritingImproveRequest>({
    mutationFn: writingApi.improve,
  });
};
