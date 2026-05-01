package hexlet.code.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Faker faker;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper om;

    @Test
    @WithMockUser
    public void testIndex() throws Exception {
        this.userRepository.deleteAll();
        User user = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        this.userRepository.save(user);
        User user2 = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        this.userRepository.save(user2);
        MvcResult result = this.mockMvc.perform(get("/api/users?_start=0&_end=10&_sort=id&_order=ASC"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Total-Count"))
            .andExpect(jsonPath("$").isArray())
            .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(2);
    }

    @Test
    @WithMockUser
    public void testShow() throws Exception {
        User user = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        user = this.userRepository.save(user);
        MvcResult result = this.mockMvc.perform(get("/api/users/" + user.getId()))
            .andExpect(status().isOk())
            .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body);
    }

    @Test
    public void testCreate() throws Exception {
        final String email = "john@example.com";
        User user = Instancio.of(User.class)
            .supply(Select.field(User::getEmail), () -> email)
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        Map<String, String> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("password", "password");
        MockHttpServletRequestBuilder request = post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));
        MvcResult result = this.mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(email))
            .andReturn();
        String body = result.getResponse().getContentAsString();
        System.out.println("Compare response testCreate: ");
        System.out.println(this.om.writeValueAsString(user));
        System.out.println(body);
    }

    @Test
    @WithMockUser
    public void testUpdate() throws Exception {
        final String email = this.faker.internet().emailAddress();
        final String lastName = this.faker.name().lastName();
        User user = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getLastName), () -> lastName)
            .supply(Select.field(User::getEmail), () -> email)
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        this.userRepository.save(user);

        HashMap<String, String> data = new HashMap<>();
        String firstName = "Mike update";
        data.put("firstName", firstName);

        MockHttpServletRequestBuilder request = put("/api/users/{id}", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.om.writeValueAsString(data));

        this.mockMvc.perform(request)
            .andExpect(status().isOk());

        User updatedUser = this.userRepository.findById(user.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo(firstName);
        assertThat(updatedUser.getLastName()).isEqualTo(lastName);
        assertThat(updatedUser.getEmail()).isEqualTo(email);
    }

    @Test
    @WithMockUser
    public void testDelete() throws Exception {
        User user = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        this.userRepository.save(user);
        MockHttpServletRequestBuilder request = delete("/api/users/" + user.getId())
            .contentType(MediaType.APPLICATION_JSON);
        this.mockMvc.perform(request)
            .andExpect(status().isNoContent());
        boolean deleted = this.userRepository.findById(user.getId()).isEmpty();
        assertThat(deleted).isEqualTo(true);
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
    @WithMockUser
    public void testUpdateWithInvalidData() throws Exception {
        User user = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
            .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
            .create();
        this.userRepository.save(user);

        HashMap<String, String> data = new HashMap<>();
        data.put("email", "");

        MockHttpServletRequestBuilder request = put("/api/users/" + user.getId())
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
