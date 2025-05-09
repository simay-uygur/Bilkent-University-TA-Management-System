package com.example.service.RequestServices;

import java.util.List;

import com.example.entity.Requests.PreferTasToCourseDto;

public interface PreferTasToCourseServ {
    boolean createRequest(List<Long> preferredReqs, List<Long> nonPreferredTas, int taNeeded, Long instrId, String sectionCode);
    List<PreferTasToCourseDto> getRequestsOfTheDeparment(String depName);
    List<PreferTasToCourseDto> getRequestsOfTheInstructor(Long instrId);
    PreferTasToCourseDto getRequestById(Long reqId);
}
