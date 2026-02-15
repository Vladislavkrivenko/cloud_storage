package spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.dto.RegisterRequestDto;
import spring.entity.UserEntity;
import spring.exeption.UsernameAlreadyExistsException;
import spring.mapper.UserMapper;
import spring.repository.UserRegistrationDao;
import spring.util.valid.UsernameAndPasswordValidation;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRegistrationDao userRegistrationDao;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public UserEntity saveUsers(RegisterRequestDto registerRequestDto) {

        UsernameAndPasswordValidation.validatePassword(registerRequestDto.password());
        UsernameAndPasswordValidation.validateUsername(registerRequestDto.username());

        if (userRegistrationDao.existsByLogin(registerRequestDto.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        UserEntity user = userMapper.toEntity(registerRequestDto);
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        return userRegistrationDao.save(user);
    }
}
