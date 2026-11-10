package com.ticket_booking.mapper;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.response.UserAuthCache;
import com.ticket_booking.dto.response.UserResponse;
import com.ticket_booking.entity.Role;
import com.ticket_booking.entity.User;
import com.ticket_booking.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
) // Quan trọng: để Spring quản lý
public interface UserMapper {

    // Map từ Entity sang DTO trả về cho client
    // Nếu tên trường khác nhau, dùng @Mapping để chỉ định

    @Mapping(source = "role.type", target = "roleType")   
    UserDto toDto(User user);

    @Mapping(source = "role.type", target = "roleType")  
    User userDtoToEntity(UserDto dto);

    @Mapping(source = "role.type", target = "roleType")  
    UserAuthCache toUserAuthDto(User user);
    
    @Mapping(target = "role", ignore = true)   
    @Mapping(target = "fullName", ignore = true)      
    @Mapping(target = "profilePicture", ignore = true) 
    @Mapping(target = "createdAt", ignore = true)     
    @Mapping(target = "updatedAt", ignore = true)      
    User userAuthCacheToUser(UserAuthCache cache);
}