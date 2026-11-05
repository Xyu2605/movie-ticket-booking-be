package com.ticket_booking.mapper;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.response.UserAuthCache;
import com.ticket_booking.dto.response.UserResponse;
import com.ticket_booking.entity.User;
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
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "role.name", target = "roleName")  
    @Mapping(target = "password", ignore = true) 
    UserResponse toResponse(User user);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "role.name", target = "roleName")  
    @Mapping(target = "password", ignore = true) 
    UserDto toDto(User user);


    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "role.name", target = "roleName")  
    @Mapping(target = "password", ignore = true) 
    UserAuthCache toUserAuthDto(User user);

    User userAuthCacheToUser(UserAuthCache userAuthCache);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "role.name", target = "roleName")  
    @Mapping(target = "password", ignore = true) 
    User userDtoToEntity(UserDto dto);
}