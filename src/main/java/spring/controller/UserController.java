package spring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.UserResponseDto;
import spring.entity.CustomUserDetails;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public UserResponseDto me(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return new UserResponseDto(user.getUsername());
    }
}
