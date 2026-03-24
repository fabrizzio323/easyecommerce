package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source="id", target="id")
    @Mapping(source="name", target="name")
    @Mapping(source="email", target="email")
    @Mapping(source="password", target="password")
    @Mapping(source="createdAt", target="createdAt")
    UserDTO toUserDTO(User user);

    @InheritConfiguration
    User toUser(UserDTO userDTO);
    List<UserDTO> toUserDTOList(List<User> users);
    List<User> toUserList(List<User> usersDTO);
}
