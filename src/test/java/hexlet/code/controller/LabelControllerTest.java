package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestUtils;
import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
public final class LabelControllerTest {
    private final WebApplicationContext wac;
    private final LabelRepository labelRepository;
    private final ObjectMapper om;
    private final TestUtils testUtils;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private Label testLabel;
    private User testUser;

    @Autowired
    public LabelControllerTest(
            WebApplicationContext wac, LabelRepository labelRepository, ObjectMapper om,
            TestUtils testUtils
    ) {
        this.wac = wac;
        this.labelRepository = labelRepository;
        this.om = om;
        this.testUtils = testUtils;
    }

    @BeforeEach
    public void setUp() {
        this.testUtils.clear();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        this.testUser = this.testUtils.createUser();
        this.token = jwt().jwt(builder -> builder.subject(this.testUser.getEmail()));
        this.testLabel = this.testUtils.createLabel();
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/labels")
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(1);
        //TODO check body
        assertThatJson(body).node("[0].createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    @Test
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/labels/{id}", this.testLabel.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(this.testLabel.getName()),
                v -> v.node("createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$")
        );
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, String> data = Map.of("name", "New Label");

        this.mockMvc.perform(post("/api/labels")
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Label label = this.labelRepository.findByName("New Label").get();
        assertThat(label.getName()).isEqualTo("New Label");
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, String> data = Map.of("name", "Updated Label");

        this.mockMvc.perform(put("/api/labels/{id}", this.testLabel.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        Label label = this.labelRepository.findById(this.testLabel.getId()).get();
        assertThat(label.getName()).isEqualTo("Updated Label");
    }

    @Test
    public void testDelete() throws Exception {
        Label label = this.testUtils.createLabel();
        this.mockMvc.perform(delete("/api/labels/{id}", label.getId())
                        .with(this.token))
                .andExpect(status().isNoContent());

        assertThat(this.labelRepository.existsById(label.getId())).isFalse();
    }

    @Test
    public void testDeleteWithTask() throws Exception {
        Label label = this.testUtils.createLabelWithUserAndTaskAndTaskStatus();
        this.mockMvc.perform(delete("/api/labels/{id}", label.getId())
                        .with(this.token))
                .andExpect(status().isConflict());

        assertThat(this.labelRepository.existsById(label.getId())).isTrue();
    }


    @Test
    public void testIndexWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }
}
