package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.Request;

@Repository
public interface RequestRepo extends JpaRepository<Request, Long>{
     /*@Query("""
        SELECT r
        FROM Request r
        WHERE r.receiver.id     = :userId
        AND r.sentTime       >= :from
        AND r.sentTime       <= :to
    """)
    List<Request> findAllReceivedByUserWithin(
        @Param("userId") Long userId,
        @Param("from")   Date from,
        @Param("to")     Date to
    );*/

}
