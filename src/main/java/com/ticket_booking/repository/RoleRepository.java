package com.ticket_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticket_booking.entity.Role;
import com.ticket_booking.enums.RoleCode;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByCode(RoleCode code);
    boolean existsByCode(RoleCode code);
}
