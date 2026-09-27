package com.actisys.userservice.mapper;

import com.actisys.userservice.dto.UserDTO;
import com.actisys.userservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
}
