package com.example.service.RequestServices;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.dto.SwapOptionDto;
import com.example.entity.Requests.SwapDto;

public interface SwapServ {
    public CompletableFuture<Boolean> createSwapRequest(SwapDto dto, Long senderId);
    public boolean acceptSwapRequest(Long requestId, Long receiverId);
    public void rejectSwapRequest(Long requestId, Long receiverId);
    public void cancelSwapRequest(Long requestId, Long senderId);
    public void updateSwapRequest(SwapDto dto, Long requestId, Long senderId);
    public void getSwapRequestById(Long requestId, Long userId);
    public CompletableFuture<List<SwapOptionDto>> findSwapCandidates(Long senderId, int senderExamId);
}
