package com.ticket_booking.security.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        
        User user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        return CustomUserDetails.buildUserDetails(user);
    }
}
