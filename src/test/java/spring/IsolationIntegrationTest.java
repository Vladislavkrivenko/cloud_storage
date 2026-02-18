package spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import spring.dto.RegisterRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IsolationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void user_shouldNotAccessAnotherUsersFile() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser1", "Password1!");

        MvcResult user1Result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Cookie user1Cookie = user1Result.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "secret.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Top Secret".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(user1Cookie)
        ).andExpect(status().isCreated());

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser2", "Password1!");

        MvcResult user2Result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user2))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Cookie user2Cookie = user2Result.getResponse().getCookie("SESSION");

        mockMvc.perform(
                get("/api/resource")
                        .param("path", "secret.txt")
                        .cookie(user2Cookie)
        ).andExpect(status().isNotFound());
    }

    @Test
    void user_shouldNotDownloadAnotherUsersFile() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser3", "Password1!");

        MvcResult user1Result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Cookie user1Cookie = user1Result.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "private.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Private Data".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(user1Cookie)
        ).andExpect(status().isCreated());

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser4", "Password1!");

        MvcResult user2Result = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user2))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Cookie user2Cookie = user2Result.getResponse().getCookie("SESSION");

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "private.txt")
                        .cookie(user2Cookie)
        ).andExpect(status().isNotFound());
    }
    @Test
    void user_shouldNotFindAnotherUsersFile_viaSearch() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser5", "Password1!");

        MvcResult r1 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1))
        ).andReturn();

        Cookie u1 = r1.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hidden.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hidden".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(u1)
        );

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser6", "Password1!");

        MvcResult r2 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2))
        ).andReturn();

        Cookie u2 = r2.getResponse().getCookie("SESSION");

        mockMvc.perform(
                        get("/api/resource/search")
                                .param("query", "hidden")
                                .cookie(u2)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]")); // пустий список
    }
    @Test
    void user_shouldNotSeeAnotherUsersFiles_inDirectoryListing() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser7", "Password1!");

        MvcResult r1 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1))
        ).andReturn();

        Cookie u1 = r1.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "private.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Secret".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(u1)
        );

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser8", "Password1!");

        MvcResult r2 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2))
        ).andReturn();

        Cookie u2 = r2.getResponse().getCookie("SESSION");

        mockMvc.perform(
                        get("/api/directory")
                                .param("path", "")
                                .cookie(u2)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
    @Test
    void user_shouldNotMoveAnotherUsersFile() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser9", "Password1!");

        MvcResult r1 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1))
        ).andReturn();

        Cookie u1 = r1.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "moveMe.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Move".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(u1)
        );

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser10", "Password1!");

        MvcResult r2 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2))
        ).andReturn();

        Cookie u2 = r2.getResponse().getCookie("SESSION");

        mockMvc.perform(
                get("/api/resource/move")
                        .param("from", "moveMe.txt")
                        .param("to", "newName.txt")
                        .cookie(u2)
        ).andExpect(status().isNotFound());
    }
    @Test
    void user_shouldNotDeleteAnotherUsersFile() throws Exception {

        RegisterRequestDto user1 =
                new RegisterRequestDto("isoUser11", "Password1!");

        MvcResult r1 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1))
        ).andReturn();

        Cookie u1 = r1.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "deleteMe.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Delete".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(u1)
        );

        RegisterRequestDto user2 =
                new RegisterRequestDto("isoUser12", "Password1!");

        MvcResult r2 = mockMvc.perform(
                post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2))
        ).andReturn();

        Cookie u2 = r2.getResponse().getCookie("SESSION");

        mockMvc.perform(
                delete("/api/resource")
                        .param("path", "deleteMe.txt")
                        .cookie(u2)
        ).andExpect(status().isNotFound());
    }
}
