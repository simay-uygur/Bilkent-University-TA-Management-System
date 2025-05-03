package com.example.service.RequestServices;

import com.example.entity.Requests.TransferProctoringDto;

public interface TransferProctoringServ {
    public void createTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
    public void updateTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
    public void deleteTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
}
