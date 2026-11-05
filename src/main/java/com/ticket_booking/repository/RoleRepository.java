package com.ticket_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket_booking.entity.Role;
import com.ticket_booking.enums.RoleType;

public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByType(RoleType type);
}
