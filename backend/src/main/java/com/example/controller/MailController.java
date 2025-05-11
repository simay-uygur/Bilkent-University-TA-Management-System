package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.MailService;
import com.example.service.UserServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;
    private final UserServ userService;

    /**
     * Sends the user's password to their email.
     *
     * Preconditions:
     * - `email` must not be null and must correspond to an existing user.
     *
     * Postconditions:
     * - Sends an email containing the user's password.
     * - Returns a boolean indicating success or failure.
     * - If the email does not correspond to an existing user, returns status 400 (BAD_REQUEST).
     * - If an error occurs while sending the email, returns status 500 (INTERNAL_SERVER_ERROR).
     *
     * @param email The email address of the user.
     * @return ResponseEntity containing a boolean value indicating success or failure.
     */
    /*@PostMapping("/send-password")
    public ResponseEntity<?> sendPassword(@RequestParam String email) {
        boolean result = false;
        String password = userService.getUserByEmail(email).getPassword();

        if (password == null) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

        String subject = "TMS Şifre Gönderimi";
        String text = "Sevgili Kullanıcı,\n\nŞifreniz: " + password + "\n\nLütfen en kısa sürede şifrenizi değiştiriniz.";

        try {
            mailService.sendMail(email, subject, text);
            result = true;
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result = false;
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}