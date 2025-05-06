package com.example.service;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.Courses.Course;
import com.example.entity.General.ClassRoom;
import com.example.entity.Tasks.Task;
import com.example.exception.NoPersistExc;
import com.example.repo.ClassRoomRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class AdminServImpl implements AdminServ{

    @Autowired
    private ClassRoomRepo classRoomRepo;

    @Override
    public HashSet<Task> getAllApprovedTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllApprovedTasks'");
    }

    @Override
    public HashSet<Task> getAllUnApprovedTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUnApprovedTasks'");
    }

    @Override
    public HashSet<Task> getAllTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTasks'");
    }

    @Override
    public boolean approveTask(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveTask'");
    }

    @Override
    public boolean deleteTask(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTask'");
    }

    @Override
    public boolean deleteUser(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public boolean deleteAdmin(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAdmin'");
    }

    @Override
    public boolean deleteTA(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTA'");
    }

    @Override
    public boolean restoreTask(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'restoreTask'");
    }

    @Override
    public boolean restoreUser(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'restoreUser'");
    }

    @Override
    public boolean updateTask(Task t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTask'");
    }

    @Override
    public HashSet<Task> getAllDeletedTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDeletedTasks'");
    }

    @Override
    public boolean createTA(TA ta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTA'");
    }

    @Override
    public boolean soft_deleteTA(TA ta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'soft_deleteTA'");
    }

    @Override
    public boolean strict_deleteTA(TA ta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'strict_deleteTA'");
    }

    @Override
    public boolean restoreTA(TA ta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'restoreTA'");
    }

    @Override
    public boolean updateTA(TA ta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTA'");
    }

    @Override
    public HashSet<TA> getAllTAs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTAs'");
    }

    @Override
    public HashSet<TA> getAllDeletedTAs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDeletedTAs'");
    }

    @Override
    public HashSet<TA> getAllNotDeletedTAs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllNotDeletedTAs'");
    }

    @Override
    public HashSet<User> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    @Override
    public boolean updateUser(User u) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public boolean createUser(User u) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public boolean soft_deleteUser(User u) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'soft_deleteUser'");
    }

    @Override
    public boolean strict_deleteUser(User u) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'strict_deleteUser'");
    }

    @Override
    public boolean createCourse(Course c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createCourse'");
    }

    @Override
    public boolean updateCourse(Course c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCourse'");
    }

    @Override
    public boolean createClassroom(ClassRoom room) {
        classRoomRepo.saveAndFlush(room);
        if (classRoomRepo.findById(room.getClassroomId()).isPresent())
            return true;
        throw new NoPersistExc("Classroom creation ");
    }
    /*{
        "class_code" : ""
        "class_capacity"   :
    }*/

    @Override
    public boolean deleteClassroom(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteClassroom'");
    }
    
}
