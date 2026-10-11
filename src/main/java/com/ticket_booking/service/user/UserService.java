package com.ticket_booking.service.user;

import org.springframework.stereotype.Service;

import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.service.redis.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class UserService implements IUserService{
    
    private final UserRepository userRepository;
    private final RedisService redisService;

}
