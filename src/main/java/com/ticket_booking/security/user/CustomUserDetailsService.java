package com.ticket_booking.security.user;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.ticket_booking.entity.User;
import com.ticket_booking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CustomUserDetailsService implements UserDetailsService{
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email){
        
        Optional<User> user = userRepository.findByEmail(email);
        return CustomUserDetails.buildUserDetails(user.get());
    }
}
