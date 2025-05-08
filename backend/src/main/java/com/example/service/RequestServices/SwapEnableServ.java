package com.example.service.RequestServices;

import com.example.entity.Requests.SwapEnableDto;

public interface SwapEnableServ {
    public void createSwapEnableReq(SwapEnableDto swapEnableDto, Long senderId);
    public void updateSwapEnableReq(SwapEnableDto swapEnableDto, Long senderId);
    public void deleteSwapEnableReq(SwapEnableDto swapEnableDto, Long senderId);
}
