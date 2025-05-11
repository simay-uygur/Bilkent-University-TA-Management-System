package com.example.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Actors.User;
import com.example.entity.General.Term;
import com.example.exception.GeneralExc;
import com.example.exception.IncorrectWebMailException;
import com.example.exception.UserExistsExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.UserRepo;
import com.example.security.JwtResponse;
import com.example.security.JwtTokenProvider;
import com.example.security.PasswordResetToken;
import com.example.security.PasswordResetTokenRepo;
import com.example.security.SignInRequest;
import com.example.security.UserDetailsImpl;
import com.example.security.UserDetailsServiceImpl;
import com.example.service.LogService;
import com.example.service.MailService;
import com.example.service.UserServ;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {    
    private final UserServ serv;
    private final UserRepo userRepo;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder; 
    private final PasswordResetTokenRepo tokenRepo;
    private final MailService mailService;
    private final LogService log;

    @PostMapping("/api/signUp")
    public ResponseEntity<User> createUser(@RequestBody User u) 
    {
        //System.out.println("role: " + u.getRole() + "id: " + u.getId());
        User user_to_check = serv.getUserById(u.getId());
        if (serv.getUserById(u.getId()) != null)
            throw new UserExistsExc(u.getId()) ;
        String[] name = u.getName().split(" ");
        String name_to_check = name[name.length-1];

        String[] surname = u.getSurname().split(" ");
        String surname_to_check = surname[surname.length-1];
        String check_mail = name_to_check.toLowerCase() + 
                            "." + 
                            surname_to_check.toLowerCase() + 
                            "@ug.bilkent.edu.tr";
        if (!check_mail.matches(u.getWebmail().toLowerCase()) && !Objects.equals(user_to_check.getId(), u.getId()))
            throw new IncorrectWebMailException() ;
        return new ResponseEntity<>(serv.createUser(u), HttpStatus.CREATED) ;
        //return ResponseEntity.created(URI.create("/signIn/{id}")).body(serv.createUser(u)) ;
    }

    @PostMapping("/api/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        if(!userRepo.existsById(request.getId())){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        // 1) Load user by ID
        UserDetailsImpl user = (UserDetailsImpl) 
            userDetailsService.loadUserByUsername(request.getId().toString());
        // 2) Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        // 3) Build an Authentication object so SecurityContext is populated
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                user, 
                null, 
                user.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 4) Generate JWT
        String jwt = tokenProvider.generateJwtToken(auth);
        // 5) Return the token + user info
        JwtResponse body = new JwtResponse(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("ROLE_USER"),
            getCurrentSemester()
            
            
        );
        log.info("Authentication", "User with id: " + body.getId() + " and ROLE: " + body.getRole() + " entered to the system.");
        return ResponseEntity.ok(body);
    }


    @GetMapping("/api/all")
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(serv.getAllUsers(), HttpStatus.OK) ;
    }

    @DeleteMapping("/api/{id}")
    public ResponseEntity<HttpStatus> deleteStudent(@PathVariable Long id) {
        User u = serv.getUserById(id) ;
        if (u == null){
            throw new UserNotFoundExc(id) ;
        }
        serv.deleteUser(u);
        log.info("Deletion", "User with id: " + u.getId() + " and ROLE: " + u.getRole() + " is deleted.");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private String getCurrentSemester(){

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        Term term;
        Month month = today.getMonth();

        switch (month) {
            // February → June  = SPRING
            case FEBRUARY:
            case MARCH:
            case APRIL:
            case MAY:
            case JUNE:
                term = Term.SPRING;
                break;

            // July & August = SUMMER
            case JULY:
            case AUGUST:
                term = Term.SUMMER;
                break;

            // September → January = FALL
            default:
                // (covers SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER, JANUARY)
                term = Term.FALL;
                break;
        }
        return year + "-" + term;
    
    }

    @PostMapping("/api/auth/request-password-reset")
    public ResponseEntity<Void> requestReset(@RequestBody RequestResetDto dto) {
        User u = userRepo.findUserByWebmail(dto.getWebmail())
            .orElseThrow(() -> new UsernameNotFoundException(dto.getWebmail()));
        // remove any old token
        tokenRepo.deleteByUser(u);

        String token = UUID.randomUUID().toString();
        var expires = LocalDateTime.now().plusHours(3);

        var prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(u);
        prt.setExpiresAt(expires);
        tokenRepo.save(prt);

        /*String link = "https://your.app/reset-password?token=" + token;
        mailService.sendMail(
            u.getWebmail(),
            "Password Reset Link",
            "Click here to reset (valid 3 h):\n\n" + link
        );*/

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/perform-password-reset")
    public ResponseEntity<Void> performReset(
        @RequestBody ResetPasswordDto dto
    ) {
        PasswordResetToken prt = tokenRepo.findByToken(dto.getToken())
            .orElseThrow(() -> new GeneralExc("Invalid token"));

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new GeneralExc("Token expired");
        }

        User u = prt.getUser();
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepo.save(u);

        // consume token so it can’t be reused
        tokenRepo.delete(prt);

        return ResponseEntity.noContent().build();
    }

    
    @Data
    public static class ResetPasswordDto {
    private String token;
    private String newPassword;
    }


    @PutMapping("api/{userId}/changePassword")
    public ResponseEntity<Boolean> changePasswordById(@PathVariable Long userId, @RequestBody ChangePasswordDto body) {
        serv.changePasswordById(body.getPassword(), userId);
        log.info("Password change", "User with id: " + userId + " changed the password in the system.");
        return ResponseEntity.noContent().build();
    }

    public static class ChangePasswordDto {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RequestResetDto {
        private String webmail;
        public String getWebmail() { return webmail; }
        public void setWebmail(String email) { this.webmail = email; }
    }
}