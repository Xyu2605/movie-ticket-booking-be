package com.ticket_booking.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ticket_booking.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails implements UserDetails{

    private UUID id;
    private String email;
    private String password;
    private Collection<GrantedAuthority> authorities;

    public static CustomUserDetails buildUserDetails(User user) {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(user.getRole().getName())
        );

        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override 
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}