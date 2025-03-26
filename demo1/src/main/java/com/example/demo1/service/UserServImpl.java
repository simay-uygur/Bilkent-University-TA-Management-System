package com.example.demo1.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo1.entity.TA;
import com.example.demo1.entity.User;
import com.example.demo1.exception.UserExistsExc;
import com.example.demo1.repo.TARepo;
import com.example.demo1.repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn= Exception.class)
@RequiredArgsConstructor
public class UserServImpl implements UserServ{

    @Autowired 
    private final UserRepo repo; 

    @Autowired
    private final TARepo taRepo;
    private final BCryptPasswordEncoder encoder ;
    
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
            }

        case DEANS_OFFICE -> {
            }

        case TEACHING_ASSISTANT -> {
                if (u instanceof TA) {
                    TA ta = (TA) u;
                    return taRepo.save(ta); 
                } else {
                    throw new IllegalArgumentException("User is not a TA instance.");
                }
            }

        case FACULTY_MEMBER -> {
            }

        case DEPARTMENT_STAFF -> {
            }

        case DEPARTMENT_CHAIR -> {
            }

        default -> throw new IllegalArgumentException("Unsupported role: " + u.getRole());
    }
    return null;
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
}
