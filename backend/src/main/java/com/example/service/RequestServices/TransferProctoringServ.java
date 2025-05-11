package com.example.service.RequestServices;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.entity.Requests.TransferCandidateDto;
import com.example.entity.Requests.TransferProctoringDto;

public interface TransferProctoringServ {
    public void createTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
    public void updateTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
    public void deleteTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId);
    public CompletableFuture<List<TransferCandidateDto>> findTransferCandidates(Long senderId, int examId) ;
    public void approveRequest(Long requestId, Long receiverId);
    public void rejectRequest(Long requestId, Long receiverId);
}
