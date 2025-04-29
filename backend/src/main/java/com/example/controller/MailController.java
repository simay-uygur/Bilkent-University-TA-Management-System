package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Actors.User;
import com.example.service.MailService;
import com.example.service.UserServ;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;
    private final UserServ userService;

    @Autowired
    public MailController(MailService mailService, UserServ userService) {
        this.mailService = mailService;
        this.userService = userService;
    }
    @PostMapping("/test")
    public ResponseEntity<String> basicTest() {
        return ResponseEntity.ok("post geldi!");
    }
    
    /**
     * Sends the user's password to their email address.
     *
     * @param email The email address of the user.
     * @return ResponseEntity containing a boolean indicating success or failure.
     */
    @PostMapping("/send-password")
    public ResponseEntity<Boolean> sendPassword(@RequestParam String email) {
        System.out.println("[DEBUG] send-password endpoint'ine istek geldi. Email: " + email);

        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                System.out.println("[DEBUG] Kullanıcı bulunamadı: " + email);
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

            System.out.println("[DEBUG] Kullanıcı bulundu: " + user.getWebmail());

            String subject = "Şifre Gönderimi";
            String text = "Merhaba,\n\nŞifreniz: " + user.getPassword() + "\n\nLütfen güvenlik için şifrenizi değiştirin.";
            mailService.sendMail(email, subject, text);

            System.out.println("[DEBUG] E-posta gönderimi başarılı.");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("[DEBUG] E-posta gönderilirken hata oluştu: " + e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test-user")
    public ResponseEntity<String> testUser(@RequestParam String email) {
        System.out.println("[DEBUG] test-user endpoint'ine istek geldi. Email: " + email);

        User user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("[DEBUG] test-user sonucu: Kullanıcı bulunamadı.");
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        System.out.println("[DEBUG] test-user sonucu: Kullanıcı bulundu - " + user.getWebmail());
        return new ResponseEntity<>("User found: " + user.getWebmail(), HttpStatus.OK);
    }
}
