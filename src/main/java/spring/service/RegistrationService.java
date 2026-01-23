package spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.dto.RegisterRequestDto;
import spring.entity.UserEntity;
import spring.exeption.UsernameAlreadyExistsException;
import spring.exeption.storageExeption.BadRequestException;
import spring.mapper.UserMapper;
import spring.repository.UserRegistrationDao;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRegistrationDao userRegistrationDao;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserEntity saveUsers(RegisterRequestDto registerRequestDto) {
        if (registerRequestDto == null
                || registerRequestDto.username() == null || registerRequestDto.username().isBlank()
                || registerRequestDto.password() == null || registerRequestDto.password().isBlank()) {
            throw new BadRequestException("Username and password must not be null or blank");
        }
        if (userRegistrationDao.existsByLogin(registerRequestDto.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        UserEntity user = userMapper.entity(registerRequestDto);
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        return userRegistrationDao.save(user);
    }
}
