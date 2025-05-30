package com.example.service.RequestServices;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Department;
import com.example.entity.Courses.Section;
import com.example.entity.General.Date;
import com.example.entity.Requests.PreferTasToCourse;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.RequestType;
import com.example.exception.GeneralExc;
import com.example.mapper.Requests.PreferTasToCourseMapper;
import com.example.repo.DepartmentRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.PreferTasToCourseRepo;
import com.example.repo.SectionRepo;
import com.example.repo.TARepo;
import com.example.service.CourseOfferingServ;
import com.example.service.LogService;
import com.example.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferTasToCourseServImpl implements PreferTasToCourseServ{

    private final DepartmentRepo depRepo;
    private final InstructorRepo instrRepo;
    private final PreferTasToCourseRepo prefRepo;
    private final PreferTasToCourseMapper mapper;
    private final TARepo taRepo;
    private final SectionRepo sectionRepo;
    private final CourseOfferingServ offeringServ;
    private final LogService log;
    private final NotificationService notServ;
    @Override
    @Transactional
    public PreferTasToCourseDto getRequestById(Long reqId) {
        PreferTasToCourse req = prefRepo.findById(reqId)
            .orElseThrow(() -> new GeneralExc("Request not found: " + reqId));
        return mapper.toDto(req);
    }
    @Override
    @Transactional
    public List<PreferTasToCourseDto> getRequestsOfTheDeparment(String depName) {
        List<PreferTasToCourse> reqs= prefRepo.findByReceiver_Name(depName).orElse(Collections.emptyList());
        return     reqs
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PreferTasToCourseDto> getRequestsOfTheInstructor(Long instrId) {
        List<PreferTasToCourse> reqs = prefRepo.findBySender_Id(instrId).orElse(Collections.emptyList());
        return     reqs
                    .stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    public boolean approve(Long reqId){
        PreferTasToCourse req = prefRepo.findById(reqId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(true);
        req.setRejected(false);
        req.setPending(false);
        log.info("Prefer Tas to Course Request Finish","Prefer Tas to Course Request with id: " +reqId+ " is finished by " +req.getReceiver().getName()+ " Department");
        prefRepo.save(req);
        notServ.notifyApproval(req);
        return true;
    }

    @Override
    public boolean reject(Long reqId){
        PreferTasToCourse req = prefRepo.findById(reqId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Prefer Tas to Course Request Finish","Prefer Tas to Course Request with id: " +reqId+ " is finished by " +req.getReceiver().getName()+ " Department");
        prefRepo.save(req);
        notServ.notifyRejection(req);
        return true;
    }

    @Override
    @Transactional
    public boolean createRequest(List<Long> preferredReqs, List<Long> nonPreferredTas, int taNeeded, Long instrId,
            String sectionCode) {
        // 1. Load instructor
        Instructor instr = instrRepo.findById(instrId)
            .orElseThrow(() -> new GeneralExc("Instructor not found: " + instrId));

        // 2. Load section (and derive course & department)
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
            .orElseThrow(() -> new GeneralExc("Section not found: " + sectionCode));
        
        String[] parts = sectionCode.split("-");
        String courseCode = parts[0] + "-" + parts[1];
        CourseOffering off = offeringServ.getCurrentOffering(courseCode);
        Course course = off.getCourse();
        Department dept = course.getDepartment();
        
        if (prefRepo.existsBySender_IdAndSection_SectionIdAndReceiver_Name(instrId, section.getSectionId(), dept.getName())) {
            throw new GeneralExc(
                "A request by instructor " + instrId +
                " for section " + sectionCode +
                " in department " + dept.getName() +
                " already exists."
            );
        }

        PreferTasToCourse req = new PreferTasToCourse();
        Date sentTime = new Date().currenDate();
        req.setSentTime(sentTime);
        req.setRequestType(RequestType.PreferTasToCourse);
        req.setDescription("Instructor with id " + instrId + " sent request to prefer or nonprefer TAs");
        req.setSender(instr);
        req.setReceiver(dept);
        req.setSection(section);
        req.setTaNeeded(taNeeded);
        req.setAmountOfAssignedTas(0);
        req.setCourseCode(courseCode);
        List<TA> preferred = taRepo.findAllById(preferredReqs);
        if (preferred.size() != preferredReqs.size()) {
            throw new GeneralExc("Some preferred TAs not found");
        }
        req.setPreferredTas(preferred);

        List<TA> nonPreferred = taRepo.findAllById(nonPreferredTas);
        if (nonPreferred.size() != nonPreferredTas.size()) {
            throw new GeneralExc("Some non-preferred TAs not found");
        }
        req.setNonPreferredTas(nonPreferred);
        log.info("Proctor Tas to the Course Request creation", "Instructor with id: " + instrId + " has sent the request for the " + sectionCode + " section to the " + dept.getName());
        prefRepo.save(req);
        notServ.notifyCreation(req);
        return true;
    }
    
}
