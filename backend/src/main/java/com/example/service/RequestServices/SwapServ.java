package com.example.service.RequestServices;

import com.example.entity.Requests.SwapDto;

public interface SwapServ {
    public void createSwapRequest(SwapDto dto, Long senderId);
    public void acceptSwapRequest(Long requestId, Long receiverId);
    public void rejectSwapRequest(Long requestId, Long receiverId);
    public void cancelSwapRequest(Long requestId, Long senderId);
    public void updateSwapRequest(SwapDto dto, Long requestId, Long senderId);
    public void getSwapRequestById(Long requestId, Long userId);
}
