package com.example.service;

import java.util.List;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Actors.DepartmentStaff;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.exception.GeneralExc;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    
    private final JavaMailSender mailSender;
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final ExamRepo examRepo;
    /**
     * Sends an email using the provided recipient, subject, and message body.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param text    The body of the email.
     */
    public void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tamanagementsystembilkent@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // you can replace with a logger if you have one
            System.err.printf("Unable to send email to %s. Reason: %s%n", to, e.getMessage());
        }
    }

    /**
     * (Optional) If you still need your old sendMail(Long...) method,
     * you can simply delegate to send():
     */
    public void sendMail(Long toUserId, String subject, String text) {
        // look up user’s email by ID, e.g. via UserRepository
        String email = userRepo.findById(toUserId).get().getWebmail();
        send(email, subject, text);
    }

    @Transactional
    @Async("notificationExecutor")
    public void notifyIfFullyStaffed(int examId) {
    // 1) Re-fetch the exam within this new TX
    Exam exam = examRepo.findById(examId)
        .orElseThrow(() -> new IllegalArgumentException("No exam " + examId));

    int required = exam.getRequiredTAs();
    int assigned = exam.getAmountOfAssignedTAs();
    if (assigned != required) return;  // not fully staffed yet

    String subject = "Exam Fully Staffed: " + exam.getDescription();

    // Safe to do lazy loads here:
    CourseOffering offering = exam.getCourseOffering();
    if (offering != null && offering.getCoordinator() != null) {
      send(
        offering.getCoordinator().getWebmail(),
        subject,
        String.format(
          "Hello %s %s,%n%nYour exam \"%s\" (ID %d) is now fully staffed with %d TAs.%n",
          offering.getCoordinator().getName(),
          offering.getCoordinator().getSurname(),
          exam.getDescription(),
          exam.getExamId(),
          assigned
        )
      );
    }

    for (TA ta : exam.getAssignedTas()) {
      if (ta.getWebmail() != null) {
        send(
          ta.getWebmail(),
          subject,
          String.format(
            "Hello %s %s,%n%nYou have been assigned to proctor \"%s\" (Exam ID %d).%n",
            ta.getName(), ta.getSurname(),
            exam.getDescription(), exam.getExamId()
          )
        );
      }
    }

    // now department staff
    Course course = offering.getCourse();
    Department dept = (course != null) 
        ? departmentRepo.findDepartmentByName(course.getDepartment().getName()).get() 
        : null;
    if (dept != null) {
      for (DepartmentStaff staff : dept.getStaff()) {
        if (staff.getWebmail() != null) {
          send(
            staff.getWebmail(),
            subject,
            String.format(
              "Hello %s %s,%n%nThe exam \"%s\" (ID %d) for your department is fully staffed.%n",
              staff.getName(), staff.getSurname(),
              exam.getDescription(), exam.getExamId()
            )
          );
        }
      }
    }
  }
}
