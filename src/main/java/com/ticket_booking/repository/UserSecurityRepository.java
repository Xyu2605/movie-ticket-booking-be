package com.ticket_booking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.ticket_booking.entity.UserSecurity;

public interface UserSecurityRepository extends JpaRepository<UserSecurity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT us
            FROM UserSecurity us
            WHERE us.userId = :userId
            """)
    Optional<UserSecurity> findByUserIdForUpdate(
            @Param("userId") UUID userId
    );
}