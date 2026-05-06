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

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestUtils;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public final class UserControllerTest {
    private final WebApplicationContext wac;
    private final Faker faker;
    private final UserRepository userRepository;
    private final TestUtils testUtils;
    private final ObjectMapper om;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private User testUser;

    @Autowired
    public UserControllerTest(
        WebApplicationContext wac, Faker faker, UserRepository userRepository,
        TestUtils testUtils, ObjectMapper om
    ) {
        this.wac = wac;
        this.faker = faker;
        this.userRepository = userRepository;
        this.testUtils = testUtils;
        this.om = om;
    }

    @BeforeEach
    public void setUp() {
        this.testUtils.clear();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        this.testUser = testUtils.createUser();
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
        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).node("[0].createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$");
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
            v -> v.node("createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$")
        );
    }

    @Test
    public void testCreate() throws Exception {
        final String email = "john@example.com";
        User user = Instancio.of(User.class)
            .supply(Select.field(User::getEmail), () -> email)
            .supply(Select.field(User::getPassword), () -> this.faker.credentials().password())
            .create();
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

    @Test
    public void testUpdate() throws Exception {
        final String email = this.testUser.getEmail();
        final String lastName = this.testUser.getLastName();

        HashMap<String, String> data = new HashMap<>();
        String firstName = "Mike update";
        data.put("firstName", firstName);

        MockHttpServletRequestBuilder request = put("/api/users/{id}", this.testUser.getId())
            .with(this.token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));

        this.mockMvc.perform(request)
            .andExpect(status().isOk());

        User updatedUser = this.userRepository.findById(this.testUser.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo(firstName);
        assertThat(updatedUser.getLastName()).isEqualTo(lastName);
        assertThat(updatedUser.getEmail()).isEqualTo(email);
    }

    @Test
    public void testUpdateAnotherAuthorizedUserFail() throws Exception {
        User anotherUser = this.testUtils.createUser();
        final String oldFirstName = this.testUser.getFirstName();

        HashMap<String, String> data = new HashMap<>();
        String firstName = "Mike update";
        data.put("firstName", firstName);

        MockHttpServletRequestBuilder request = put("/api/users/{id}", this.testUser.getId())
            .with(jwt().jwt(builder -> builder.subject(anotherUser.getEmail())))
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
        User anotherUser = this.testUtils.createUser();
        this.mockMvc.perform(delete("/api/users/" + this.testUser.getId())
                .with(jwt().jwt(builder -> builder.subject(anotherUser.getEmail()))))
                .andExpect(status().isForbidden());
        assertThat(this.userRepository.existsById(this.testUser.getId())).isTrue();
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        User user = Instancio.of(User.class)
            .supply(Select.field(User::getEmail), () -> "")
            .create();
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
