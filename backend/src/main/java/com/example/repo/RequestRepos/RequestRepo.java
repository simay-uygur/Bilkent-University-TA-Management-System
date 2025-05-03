package com.example.repo.RequestRepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.User;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;

@Repository
public interface RequestRepo extends JpaRepository<Request, Long>{
    List<Request> findByRequestType(RequestType requestType ); // Find all requests of a specific type
    List<Request> findBySender(User sender);
    List<Request> findByReceiver(User receiver);
    List<Request> findBySenderAndRequestType(User sender, RequestType requestType ); // Find all requests of a specific type sent by a specific user
    List<Request> findByReceiverAndRequestType(User receiver, RequestType requestType ); // Find all requests of a specific type received by a specific user
    List<Request> findBySenderAndReceiver(User sender, User receiver); // Find all requests sent by a specific user to another specific user
    List<Request> findBySenderAndReceiverAndRequestType(User sender, User receiver, RequestType requestType ); // Find all requests of a specific type sent by a specific user to another specific user
}
