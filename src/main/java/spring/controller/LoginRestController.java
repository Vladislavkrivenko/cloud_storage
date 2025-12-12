package spring.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.dto.LoginRequestDto;
import spring.dto.UserResponseDto;
import spring.entity.CustomUserDetails;
import spring.entity.UserEntity;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginRestController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody LoginRequestDto dto, HttpServletRequest request) {
        if (dto.username() == null || dto.username().isBlank() || dto.password() == null || dto.password().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username and password must not be null or blank"));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true);

            String username;
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails cud) {
                UserEntity user = cud.getUser();
                username = user.getLogin();
            } else if (principal instanceof UserDetails ud) {
                username = ud.getUsername();
            } else {
                username = dto.username();
            }

            return ResponseEntity.ok(new UserResponseDto(username));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }
}
