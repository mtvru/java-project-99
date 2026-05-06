package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestUtils;
import hexlet.code.dto.AuthRequestDTO;
import hexlet.code.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public final class AuthenticationControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final TestUtils testUtils;

    @Autowired
    public AuthenticationControllerTest(
        MockMvc mockMvc, ObjectMapper om, TestUtils testUtils
    ) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.testUtils = testUtils;
    }

    private User testUser;

    @BeforeEach
    public void setUp() {
        this.testUtils.clear();
        this.testUser = this.testUtils.createUser();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername(this.testUser.getEmail());
        authRequest.setPassword("password");

        this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(authRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginFailure() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername(this.testUser.getEmail());
        authRequest.setPassword("wrong-password");

        this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
