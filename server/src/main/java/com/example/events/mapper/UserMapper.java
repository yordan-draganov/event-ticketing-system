package com.example.events.mapper;

import com.example.events.DTO.SignupRequest;
import com.example.events.DTO.UserDTO;
import com.example.events.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(SignupRequest signupRequest);
}