package com.ticket_booking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>{
    @EntityGraph(attributePaths = "role")
    @Query("""
        SELECT u
        FROM User u
        WHERE u.email = :email
    """)
    Optional<User> findByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = "role")
    @Query( """
        SELECT u 
        FROM User u 
        WHERE u.email = :email
        AND u.isActive = true
    """)    
    Optional<User> findByEmailAndActiveTrue(@Param("email") String email);

    @Query("""
        SELECT COUNT(u) > 0
        FROM User u
        WHERE u.email = :email
    """)
    boolean existsByEmail(@Param("email") String email);


    
    
}
