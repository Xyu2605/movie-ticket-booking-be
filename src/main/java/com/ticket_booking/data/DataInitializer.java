package com.ticket_booking.data;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.entity.Role;
import com.ticket_booking.enums.RoleCode;
import com.ticket_booking.repository.RoleRepository;
import com.ticket_booking.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent>{
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event){
        Map<RoleCode, String> roles = new EnumMap<>(RoleCode.class);
        roles.put(RoleCode.ADMIN,"Quản trị viên");
        roles.put(RoleCode.USER, "Người dùng");
        roles.put(RoleCode.STAFF, "Nhân viên");
        createDefaultRoleIfNotExist(roles);
    }


    private void createDefaultRoleIfNotExist(Map<RoleCode, String> roles){
        for(Map.Entry<RoleCode, String> entry : roles.entrySet()){
            RoleCode code = entry.getKey();
            String name = entry.getValue();
            if(!roleRepository.existsByCode(code)){
                Role role = new Role();
                role.setCode(code);
                role.setName(name);
                roleRepository.save(role);
            }
        }
    }
}
