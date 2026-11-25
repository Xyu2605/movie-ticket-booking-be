
package com.ticket_booking.mapper;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(source = "role.code", target = "roleCode")
    UserDto toDto(User user);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userSecurity", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User userDtoToEntity(UserDto dto);
}
