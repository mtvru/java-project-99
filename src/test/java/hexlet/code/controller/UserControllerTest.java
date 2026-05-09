package hexlet.code.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public final class UserControllerTest extends AbstractControllerTest {
    private final WebApplicationContext wac;
    private final Faker faker;
    private final UserRepository userRepository;
    private final TestPersistenceManager testPersistenceManager;
    private final TestModelFactory testModelFactory;
    private final ObjectMapper om;
    private final UserMapper mapper;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private User testUser;

    @Autowired
    public UserControllerTest(
            WebApplicationContext wac, Faker faker, UserRepository userRepository,
            TestPersistenceManager testPersistenceManager, TestModelFactory testModelFactory, ObjectMapper om,
            UserMapper mapper
    ) {
        this.wac = wac;
        this.faker = faker;
        this.userRepository = userRepository;
        this.testPersistenceManager = testPersistenceManager;
        this.testModelFactory = testModelFactory;
        this.om = om;
        this.mapper = mapper;
    }

    @BeforeEach
    public void setUp() {
        this.testPersistenceManager.clear();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        this.testUser = testModelFactory.createUser();
        this.testUser = this.testPersistenceManager.save(this.testUser);
        this.token = jwt().jwt(builder -> builder.subject(this.testUser.getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/users?_start=0&_end=10&_sort=id&_order=ASC")
                .with(this.token))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Total-Count"))
            .andExpect(jsonPath("$").isArray())
            .andReturn();
        String body = result.getResponse().getContentAsString();
        List<UserDTO> dtos = this.om.readValue(body, new TypeReference<>() { });
        List<User> actual = dtos.stream().map(this.mapper::map).toList();
        List<User> expected = this.userRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(actual.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).isEqualTo(
                expected.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }

    @Test
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/users/" + this.testUser.getId())
                .with(this.token))
            .andExpect(status().isOk())
            .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
            v -> v.node("email").isEqualTo(this.testUser.getEmail()),
            v -> v.node("createdAt").isEqualTo(
                        this.testUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
        );
    }

    @Test
    public void testCreate() throws Exception {
        final String email = "john@example.com";
        User user = this.testModelFactory.createUser(email, this.faker.credentials().password());
        Map<String, String> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("password", "password");
        MockHttpServletRequestBuilder request = post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));
        this.mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(email))
            .andReturn();
    }

    private static Stream<Arguments> partialUpdateParams() {
        return Stream.of(
                Arguments.of("firstName", "Mike update"),
                Arguments.of("lastName", "Smith update"),
                Arguments.of("email", "mike_update@example.com"),
                Arguments.of("password", "new_password")
        );
    }

    @ParameterizedTest
    @MethodSource("partialUpdateParams")
    public void testPartialUpdate(String key, Object value) throws Exception {
        Map<String, Object> data = Map.of(key, value);
        String oldFirstName = this.testUser.getFirstName();
        String oldLastName = this.testUser.getLastName();
        String oldEmail = this.testUser.getEmail();
        String oldPasswordDigest = this.testUser.getPassword();
        this.mockMvc.perform(put("/api/users/{id}", this.testUser.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());
        User updatedUser = this.userRepository.findById(this.testUser.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo(data.getOrDefault("firstName", oldFirstName));
        assertThat(updatedUser.getLastName()).isEqualTo(data.getOrDefault("lastName", oldLastName));
        assertThat(updatedUser.getEmail()).isEqualTo(data.getOrDefault("email", oldEmail));
        if (data.containsKey("password")) {
            assertThat(updatedUser.getPassword()).isNotEqualTo(oldPasswordDigest);
        } else {
            assertThat(updatedUser.getPassword()).isEqualTo(oldPasswordDigest);
        }
    }

    @Test
    public void testUpdateAnotherAuthorizedUserFail() throws Exception {
        User anotherUser = this.testModelFactory.createUser();
        anotherUser = this.testPersistenceManager.save(anotherUser);
        User finalAnotherUser = anotherUser;
        final String oldFirstName = this.testUser.getFirstName();

        HashMap<String, String> data = new HashMap<>();
        String firstName = "Mike update";
        data.put("firstName", firstName);

        MockHttpServletRequestBuilder request = put("/api/users/{id}", this.testUser.getId())
            .with(jwt().jwt(builder -> builder.subject(finalAnotherUser.getEmail())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));
        this.mockMvc.perform(request)
            .andExpect(status().isForbidden());
        User notUpdatedUser = this.userRepository.findById(this.testUser.getId()).get();
        assertThat(notUpdatedUser.getFirstName()).isEqualTo(oldFirstName);
    }

    @Test
    public void testDelete() throws Exception {
        MockHttpServletRequestBuilder request = delete("/api/users/" + this.testUser.getId())
            .with(this.token)
            .contentType(MediaType.APPLICATION_JSON);
        this.mockMvc.perform(request)
            .andExpect(status().isNoContent());
        boolean deleted = this.userRepository.findById(this.testUser.getId()).isEmpty();
        assertThat(deleted).isEqualTo(true);
    }

    @Test
    public void testDeleteAnotherAuthorizedUserFail() throws Exception {
        User anotherUser = this.testModelFactory.createUser();
        anotherUser = this.testPersistenceManager.save(anotherUser);
        User finalAnotherUser = anotherUser;
        this.mockMvc.perform(delete("/api/users/" + this.testUser.getId())
                .with(jwt().jwt(builder -> builder.subject(finalAnotherUser.getEmail()))))
                .andExpect(status().isForbidden());
        assertThat(this.userRepository.existsById(this.testUser.getId())).isTrue();
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        User user = this.testModelFactory.createUser("", "");
        MockHttpServletRequestBuilder request = post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(user));
        this.mockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.put("email", "");

        MockHttpServletRequestBuilder request = put("/api/users/" + this.testUser.getId())
            .with(this.token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));

        this.mockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }
}
