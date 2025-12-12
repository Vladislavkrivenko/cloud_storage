package spring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.RegisterRequestDto;
import spring.dto.UserResponseDto;
import spring.entity.UserEntity;
import spring.service.RegistrationService;
import spring.util.valid.UsernameAndPasswordValidation;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationRestController {
    private final RegistrationService registrationService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody RegisterRequestDto dto) {
        UsernameAndPasswordValidation.validatePassword(dto.password());
        UsernameAndPasswordValidation.validateUsername(dto.username());
        UserEntity saved = registrationService.saveUsers(dto);
        UserResponseDto body = new UserResponseDto(saved.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
