package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.exception.GeneralExc;
import com.example.exception.UserExistsExc;
import com.example.exception.UserNotFoundExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn= Exception.class)
@RequiredArgsConstructor
public class UserServImpl implements UserServ{


    private final UserRepo repo; 
    private final TARepo taRepo;
    private final PasswordEncoder encoder;
    
    @Override
    public List<User> getAllUsers(){
        return repo.findAll();
    }
    @Override
    public User createUser(User u) {
        if (repo.findById(u.getId()).isPresent()) {
            throw new UserExistsExc(u.getId());
        }
    
        String hashedPass = encoder.encode(u.getPassword());
        u.setPassword(hashedPass);
    
        switch (u.getRole()) {
            case ADMIN -> {
                return repo.save(u);
            }
            case DEANS_OFFICE -> {
                return repo.save(u);
            }
            case TA -> {
                if (u instanceof TA) {
                    TA ta = (TA) u;
                    return taRepo.save(ta); 
                } else {
                    throw new GeneralExc("User is not a TA instance.");
                }
            }
            case INSTRUCTOR -> {
                // Add return statement here
                return repo.save(u);
            }
            case DEPARTMENT_STAFF -> {
                // Add return statement here
                return repo.save(u);
            }
            default -> throw new GeneralExc("Unsupported role: " + u.getRole());
        }
        // This line should never be reached, but you can leave it as a fallback
        // return null;
    }
    
    @Override
    public void deleteUser(User u) {
        u.setDeleted(true);
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> u = repo.findById(id) ;
        return u.orElse(null);
    }
    @Override
    public User getUserByEmail(String email) {
        return repo.findUserByWebmail(email).orElse(null);
    }
    @Override
    public boolean changePasswordById(String password, Long userId) {
        User user = repo.findById(userId)
        .orElseThrow(() -> new UserNotFoundExc(userId));

        String hashed = encoder.encode(password);
        user.setPassword(hashed);
        repo.save(user);
        return true;
    }
    
}
