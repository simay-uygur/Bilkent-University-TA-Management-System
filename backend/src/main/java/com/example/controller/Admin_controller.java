package com.example.controller;

import com.example.service.AdminServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.General.ClassRoom;
import com.example.service.AdminServImpl;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class Admin_controller{

    @Autowired
    private AdminServ adminServ ;

    @PostMapping("api/admin/room")
    public ResponseEntity<?> createClassroom(@RequestBody ClassRoom room) {
        return new ResponseEntity<>(adminServ.createClassroom(room), HttpStatus.CREATED);
    }
    /*{
        "class_code" : ""
        "class_capacity"   :
    } */

}
