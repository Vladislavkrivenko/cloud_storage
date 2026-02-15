package spring;

import com.fasterxml.jackson.databind.ObjectMapper; // ✅ правильний Jackson

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.testcontainers.junit.jupiter.Testcontainers;

import spring.dto.LoginRequestDto;
import spring.dto.RegisterRequestDto;
import spring.entity.UserEntity;
import spring.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // ✅ post(), get()

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // ✅ status(), jsonPath(), cookie()

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void signUp_shouldCreateUser_andReturnSessionCookie() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("authUser1", "Password1!");

        MvcResult result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("authUser1"))
                .andExpect(cookie().exists("SESSION"))
                .andReturn();

        Optional<UserEntity> saved =
                userRepository.findByLogin("authUser1");

        assertThat(saved).isPresent();
    }

    @Test
    void signIn_shouldAuthenticate_andReturnSessionCookie() throws Exception {

        RegisterRequestDto register =
                new RegisterRequestDto("authUser2", "Password1!");

        mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register))
        );

        LoginRequestDto login =
                new LoginRequestDto("authUser2", "Password1!");

        mockMvc.perform(
                        post("/api/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authUser2"))
                .andExpect(cookie().exists("SESSION"));
    }

    @Test
    void me_shouldReturn401_whenNotAuthenticated() throws Exception {

        mockMvc.perform(
                        get("/api/user/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturnUser_whenAuthenticated() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("authUser3", "Password1!");

        MvcResult result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie = result.getResponse().getCookie("SESSION");

        mockMvc.perform(
                        get("/api/user/me")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authUser3"));
    }

    @Test
    void logout_shouldInvalidateSession() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("authUser4", "Password1!");

        MvcResult result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie = result.getResponse().getCookie("SESSION");

        mockMvc.perform(
                        post("/api/auth/sign-out")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/user/me")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isUnauthorized());
    }
}
