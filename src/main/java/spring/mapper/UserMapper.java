package spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.dto.LoginRequestDto;
import spring.dto.RegisterRequestDto;
import spring.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "login", target = "username")
    LoginRequestDto dto(UserEntity user);

    @Mapping(source = "username", target = "login")
    UserEntity entity(LoginRequestDto dto);

    @Mapping(source = "login", target = "username")
    RegisterRequestDto registerDto(UserEntity user);

    @Mapping(source = "username", target = "login")
    UserEntity entity(RegisterRequestDto dto);
}
