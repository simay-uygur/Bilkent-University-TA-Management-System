package com.example.service;

import java.util.List;

import com.example.entity.Actors.User;
import com.example.entity.Requests.Request;

public interface RequestServ {
    public Request getRequestById(Long req_id);
    public List<Request> getAllRequests();
    public List<Request> getRequestsOfTheUser(User u);
    public List<Request> getSwapRequestsOfTheUser(User u); //Swap
    public List<Request> getLeaveRequestsOfTheUser(User u); // Leave
    public List<Request> getTaInFacultyRequestsOfTheUser(User u); // TA in Faculty
    public List<Request> getSwapEnableLeaveRequestsOfTheUser(User u); // Swap Enable
    public List<Request> getTransferProctoringRequestsOfTheUser(User u); // Transfer Proctoring
    public List<Request> getVolunteerProctoringRequestsOfTheUser(User u); // Volunteer Proctoring
    public List<Request> getTaFromOtherFacultiesRequestsOfTheUser(User u); // Ta from other Faculties
    public List<Request> getProctorTaFromFacultiesRequestsOfTheUser(User u); // Ta from Faculties

    public boolean createRequest(Request req);
}
