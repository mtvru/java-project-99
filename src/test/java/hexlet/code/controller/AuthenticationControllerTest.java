package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
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
public final class AuthenticationControllerTest extends AbstractControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final TestPersistenceManager testPersistenceManager;
    private final TestModelFactory testModelFactory;

    @Autowired
    public AuthenticationControllerTest(
            MockMvc mockMvc, ObjectMapper om, TestPersistenceManager testPersistenceManager,
            TestModelFactory testModelFactory
    ) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.testPersistenceManager = testPersistenceManager;
        this.testModelFactory = testModelFactory;
    }

    private User testUser;

    @BeforeEach
    public void setUp() {
        this.testPersistenceManager.clear();
        this.testUser = this.testModelFactory.createUser();
        this.testUser = this.testPersistenceManager.save(this.testUser);
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
