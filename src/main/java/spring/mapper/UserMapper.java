package spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.dto.RegisterRequestDto;
import spring.dto.UserResponseDto;
import spring.entity.UserEntity;
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "username", target = "login")
    UserEntity toEntity(RegisterRequestDto dto);

    @Mapping(source = "login", target = "username")
    UserResponseDto toResponse(UserEntity entity);
}