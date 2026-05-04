package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthRequestDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public final class AuthenticationControllerTest {
    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper om;
    private final Faker faker;

    @Autowired
    public AuthenticationControllerTest(
        MockMvc mockMvc, UserRepository userRepository,
        PasswordEncoder passwordEncoder, ObjectMapper om, Faker faker
    ) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.om = om;
        this.faker = faker;
    }

    private User testUser;

    @BeforeEach
    public void setUp() {
        this.testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> this.passwordEncoder.encode("password"))
                .create();
        this.userRepository.save(this.testUser);
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
