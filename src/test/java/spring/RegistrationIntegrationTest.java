package spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import spring.dto.RegisterRequestDto;
import spring.entity.UserEntity;
import spring.exeption.UsernameAlreadyExistsException;
import spring.repository.UserRepository;
import spring.service.RegistrationService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@Rollback
public class RegistrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDetailsService userDetailsService;

    @Test
    void shouldCreateUser() {

        RegisterRequestDto dto =
                new RegisterRequestDto("userTest1", "Password1!");

        UserEntity saved = registrationService.saveUsers(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLogin()).isEqualTo("userTest1");

        Optional<UserEntity> fromDb =
                userRepository.findByLogin("userTest1");

        assertThat(fromDb).isPresent();
    }

    @Test
    void shouldThrowIfUsernameAlreadyExists() {

        RegisterRequestDto dto =
                new RegisterRequestDto("userTest2", "Password1!");

        registrationService.saveUsers(dto);

        assertThatThrownBy(() ->
                registrationService.saveUsers(dto)
        ).isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void passwordShouldBeEncoded() {

        RegisterRequestDto dto =
                new RegisterRequestDto("userTest3", "Password1!");

        UserEntity saved = registrationService.saveUsers(dto);

        assertThat(saved.getPassword()).isNotEqualTo("Password1!");
    }

    @Test
    void shouldLoadUserByUsername() {
        RegisterRequestDto dto = new RegisterRequestDto("secUser", "Password1!");
        registrationService.saveUsers(dto);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername("secUser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("secUser");
    }
}
