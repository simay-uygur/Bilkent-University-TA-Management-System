package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email using the provided recipient, subject, and message body.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param text The body of the email.
     */
    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tamanagementsystembilkent@gmail.com"); 
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            System.out.println(String.format("Unable to send email to %s. Reason: %s", to, e.getMessage()));
        }
    }
}
