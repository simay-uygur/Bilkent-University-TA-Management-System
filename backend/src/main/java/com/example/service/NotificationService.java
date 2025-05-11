package com.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Actors.DepartmentStaff;
import com.example.entity.Courses.Department;
import com.example.entity.Notification;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.PreferTasToCourse;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.WorkLoad;
import com.example.repo.NotificationRepos;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepos notificationRepos;
    private final LogService log; 
    private final MailService mailService;

    @Transactional
    @Async("notificationExecutor")
    public void sendNotification(String text, Long receiverId) {
        Notification notif = new Notification(text, receiverId, LocalDateTime.now());
        notificationRepos.save(notif);
    }

    @Transactional(readOnly = true)
    public List<String> getNotificationsForUser(Long userId) {
        return notificationRepos
            .findAllByReceiverIdOrderByTimestampDesc(userId)
            .stream()
            .map(Notification::getText)
            .collect(Collectors.toList());
    }

    @Transactional
    @Async("notificationExecutor")
    public void sendNotificationWithMail(String text, Long receiverId, String webmail) {
        Notification notif = new Notification(text, receiverId, LocalDateTime.now());
        notificationRepos.save(notif);
    }

    @Async("notificationExecutor")
    @Transactional
    public void notifyCreation(Object req) {
        Long receiverId;
        String type   = req.getClass().getSimpleName();
        Long reqId    = extractId(req);

        if (req instanceof WorkLoad r) {
            receiverId = r.getReceiver().getId();
        }
        else if (req instanceof Leave r) {
            for (DepartmentStaff staff : ((Leave) req).getReceiver().getStaff()){
                String text = String.format("New %s request (ID=%d) submitted to you.", type, reqId);
                sendNotification(text, staff.getId());
            }
            return;
        }
        else if (req instanceof TransferProctoring r) {
            receiverId = r.getReceiver().getId();
        }
        else if (req instanceof Swap r) {
            receiverId = r.getReceiver().getId();
        }
        else if (req instanceof ProctorTaInFaculty r) {
            receiverId = r.getReceiver().getId();
        }
        else if (req instanceof ProctorTaInDepartment r) {
            // Department has no numeric ID; using hashcode fallback
            receiverId = (long) r.getReceiver().getName().hashCode();
        }
        else if (req instanceof ProctorTaFromOtherFaculty r) {
            receiverId = r.getReceiver().getId();
        }
        else if (req instanceof ProctorTaFromFaculties r) {
            // notify each sub‐receiver
            for (var sub : r.getProctorTaFromOtherFacs()) {
                String msg = String.format("New %s (ID=%d) for exam %d", 
                    type, reqId, r.getExam().getExamId());
                sendNotification(msg, sub.getReceiver().getId());
            }
            return;
        }
        else if (req instanceof PreferTasToCourse r) {
            receiverId = (long) r.getReceiver().getName().hashCode();
        }
        else {
            throw new IllegalArgumentException("Unsupported request: " + type);
        }

        String text = String.format("New %s request (ID=%d) submitted to you.", type, reqId);
        sendNotification(text, receiverId);
    }

    @Async("notificationExecutor")
    @Transactional
    public void notifyApproval(Object req) {
        Long senderId;
        String type = req.getClass().getSimpleName();
        Long reqId  = extractId(req);

        if (req instanceof WorkLoad r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof Leave r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof TransferProctoring r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof Swap r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof ProctorTaInFaculty r) {
            Department dep = r.getSender();
            for (DepartmentStaff staff: dep.getStaff()) {
                String msg = String.format("New %s (ID=%d) for exam %d", 
                    type, reqId, r.getExam().getExamId());
                sendNotification(msg, staff.getId());
            }
            return;
        }
        else if (req instanceof ProctorTaInDepartment r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof ProctorTaFromOtherFaculty r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof ProctorTaFromFaculties r) {
            senderId = r.getSender().getId();
        }
        else if (req instanceof PreferTasToCourse r) {
            senderId = r.getSender().getId();
        }
        else {
            throw new IllegalArgumentException("Unsupported request: " + type);
        }

        String text = String.format("Your %s request (ID=%d) has been approved.", type, reqId);
        sendNotification(text, senderId);
    }

    @Async("notificationExecutor")
    @Transactional
    public void notifyRejection(Object req) {
        Long senderId;
        String type = req.getClass().getSimpleName();
        Long reqId  = extractId(req);
        String text;
        if (req instanceof WorkLoad r) {
            senderId = r.getSender().getId();
            text = String.format("Your %s request (ID=%d) has been rejected.", type, reqId);
        }
        else if (req instanceof Leave r) {
            senderId = r.getSender().getId();
            text = String.format("Your %s request (ID=%d) has been rejected.", type, reqId);
        }
        else if (req instanceof TransferProctoring r) {
            senderId = r.getSender().getId();
            text = String.format("Your %s request (ID=%d) has been rejected.", type, reqId);
        }
        else if (req instanceof Swap r) {
            senderId = r.getSender().getId();
            text = String.format("Your %s request (ID=%d) has been rejected.", type, reqId);
        }
        else if (req instanceof ProctorTaInFaculty r) {
            Department dep = r.getSender();
            for (DepartmentStaff staff: dep.getStaff()) {
                text = String.format("Your %s request (ID=%d) has been finished.", type, reqId);
                sendNotification(text, staff.getId());
            }
            return;
        }
        else if (req instanceof ProctorTaInDepartment r) {
            text = String.format("Your %s request (ID=%d) has been finished.", type, reqId);
            senderId = r.getSender().getId();
        }
        else if (req instanceof ProctorTaFromOtherFaculty r) {
            text = String.format("Your %s request (ID=%d) has been finished.", type, reqId);
            senderId = r.getSender().getId();
        }
        else if (req instanceof ProctorTaFromFaculties r) {
            text = String.format("Your %s request (ID=%d) has been finished.", type, reqId);
            senderId = r.getSender().getId();
        }
        else if (req instanceof PreferTasToCourse r) {
            text = String.format("Your %s request (ID=%d) has been finished.", type, reqId);
            senderId = r.getSender().getId();
        }
        else {
            throw new IllegalArgumentException("Unsupported request: " + type);
        }
        sendNotification(text, senderId);
    }

    private Long extractId(Object req) {
        if (req instanceof Request r) {
            return r.getRequestId();
        }
        throw new IllegalArgumentException(
          "Cannot extract ID from non-Request type: " + req.getClass().getName()
        );
    }
}