
package com.ticket_booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.ticket_booking.dto.UserSecurityDto;
import com.ticket_booking.entity.UserSecurity;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserSecurityMapper {

    // UserSecurity.user không có trong DTO.
    // userId được MapStruct tự động ánh xạ.
    UserSecurityDto toDto(UserSecurity userSecurity);

    // Không ánh xạ quan hệ User trực tiếp.
    @Mapping(target = "user", ignore = true)
    UserSecurity toEntity(UserSecurityDto dto);
}
