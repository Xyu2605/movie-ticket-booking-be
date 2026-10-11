package com.ticket_booking.utils.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ticket_booking.entity.User;

public class CustomOAuth2User implements OAuth2User{
    
    private User user;
    private Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }

    public User getUser(){
        return user;
    }

	@Override
	public Map<String, Object> getAttributes() {
        return attributes;
	}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()));
    }

    @Override 
    public String getName(){
        return user.getEmail();
    }

    public String getFullName(){
        return user.getFullName();
    }

    public UUID getUserId(){
        return user.getId();
    }

}
