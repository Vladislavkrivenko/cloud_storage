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
import org.testcontainers.junit.jupiter.Testcontainers;
import spring.dto.RegisterRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void upload_shouldStoreFile_andReturnMetadata() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser1", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Cookie sessionCookie = registerResult.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                        multipart("/api/resource")
                                .file(file)
                                .param("path", "")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("hello.txt"))
                .andExpect(jsonPath("$[0].type").value("FILE"))
                .andExpect(jsonPath("$[0].size").value(11));
    }

    @Test
    void download_shouldReturnUploadedFileContent() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser2", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        MvcResult downloadResult = mockMvc.perform(
                        get("/api/resource/download")
                                .param("path", "hello.txt")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andReturn();

        String content =
                downloadResult.getResponse().getContentAsString();

        assertThat(content).isEqualTo("Hello world");
    }

    @Test
    void listDirectory_shouldReturnUploadedFile() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser3", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/directory")
                                .param("path", "")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("hello.txt"))
                .andExpect(jsonPath("$[0].type").value("FILE"))
                .andExpect(jsonPath("$[0].size").value(11));
    }

    @Test
    void remove_shouldDeleteFile() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser4", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                delete("/api/resource")
                        .param("path", "hello.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "hello.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isNotFound());
    }

    @Test
    void createDirectory_shouldCreateFolder() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser5", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        mockMvc.perform(
                        post("/api/directory")
                                .param("path", "docs")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("docs/"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"))
                .andExpect(jsonPath("$.size").value(org.hamcrest.Matchers.nullValue()));

        mockMvc.perform(
                        get("/api/directory")
                                .param("path", "")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("docs/"))
                .andExpect(jsonPath("$[0].type").value("DIRECTORY"));
    }

    @Test
    void moveFile_shouldRenameFile() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser6", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/resource/move")
                                .param("from", "hello.txt")
                                .param("to", "new.txt")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new.txt"))
                .andExpect(jsonPath("$.type").value("FILE"));

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "hello.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isNotFound());

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "new.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isOk());
    }

    @Test
    void moveFile_shouldMoveIntoDirectory() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser7", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        mockMvc.perform(
                post("/api/directory")
                        .param("path", "docs")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "hello.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello world".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/resource/move")
                                .param("from", "hello.txt")
                                .param("to", "docs")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("hello.txt"))
                .andExpect(jsonPath("$.type").value("FILE"));

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "hello.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isNotFound());

        mockMvc.perform(
                get("/api/resource/download")
                        .param("path", "docs/hello.txt")
                        .cookie(sessionCookie)
        ).andExpect(status().isOk());
    }

    @Test
    void moveDirectory_shouldMoveDirectoryWithAllFiles() throws Exception {

        RegisterRequestDto dto =
                new RegisterRequestDto("fileUser8", "Password1!");

        MvcResult registerResult = mockMvc.perform(
                        post("/api/auth/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andReturn();

        Cookie sessionCookie =
                registerResult.getResponse().getCookie("SESSION");

        mockMvc.perform(
                post("/api/directory")
                        .param("path", "docs/")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        MockMultipartFile file =
                new MockMultipartFile(
                        "object",
                        "a.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "Content A".getBytes()
                );

        mockMvc.perform(
                multipart("/api/resource")
                        .file(file)
                        .param("path", "docs/")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/directory")
                        .param("path", "archive/")
                        .cookie(sessionCookie)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                get("/api/resource/move")
                        .param("from", "docs/")
                        .param("to", "archive/")
                        .cookie(sessionCookie)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/directory")
                        .param("path", "docs/")
                        .cookie(sessionCookie)
        ).andExpect(status().isNotFound());

        mockMvc.perform(
                        get("/api/resource")
                                .param("path", "archive/docs/a.txt")
                                .cookie(sessionCookie)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("a.txt"));
    }
}
