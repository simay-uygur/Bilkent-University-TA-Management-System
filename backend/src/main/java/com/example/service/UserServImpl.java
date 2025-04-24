package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.exception.GeneralExc;
import com.example.exception.UserExistsExc;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn= Exception.class)
@RequiredArgsConstructor
public class UserServImpl implements UserServ{

    @Autowired 
    private UserRepo repo; 

    @Autowired
    private TARepo taRepo;

    @Autowired
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

        case TA -> {
                if (u instanceof TA) {
                    TA ta = (TA) u;
                    return taRepo.save(ta); 
                } else {
                    throw new GeneralExc("User is not a TA instance.");
                }
            }

        /*case FACULTY_MEMBER -> {
            }*/

        case DEPARTMENT_STAFF -> {
            }

        case DEPARTMENT_CHAIR -> {
            }

        default -> throw new GeneralExc("Unsupported role: " + u.getRole());
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
