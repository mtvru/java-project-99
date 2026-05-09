package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
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
public final class LabelControllerTest extends AbstractControllerTest {
    private final WebApplicationContext wac;
    private final LabelRepository labelRepository;
    private final ObjectMapper om;
    private final TestPersistenceManager testPersistenceManager;
    private final TestModelFactory testModelFactory;
    private final LabelMapper mapper;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private Label testLabel;
    private User testUser;

    @Autowired
    public LabelControllerTest(
            WebApplicationContext wac, LabelRepository labelRepository, ObjectMapper om,
            TestPersistenceManager testPersistenceManager, LabelMapper labelMapper, TestModelFactory testModelFactory
    ) {
        this.wac = wac;
        this.labelRepository = labelRepository;
        this.om = om;
        this.testPersistenceManager = testPersistenceManager;
        this.mapper = labelMapper;
        this.testModelFactory = testModelFactory;
    }

    @BeforeEach
    public void setUp() {
        this.testPersistenceManager.clear();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        this.testUser = this.testModelFactory.createUser();
        this.testUser = this.testPersistenceManager.save(this.testUser);
        this.token = jwt().jwt(builder -> builder.subject(this.testUser.getEmail()));
        this.testLabel = this.testModelFactory.createLabel();
        this.testLabel = this.testPersistenceManager.save(this.testLabel);
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/labels")
                .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        List<LabelDTO> labelsDto = om.readValue(body, new TypeReference<>() { });
        List<Label> actual = labelsDto.stream().map(this.mapper::map).toList();
        List<Label> expected = this.labelRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(actual.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT)))
            .isEqualTo(
                    expected.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
            );
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
                v -> v.node("createdAt").isEqualTo(
                        this.testLabel.getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
                )
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
        Label label = this.testModelFactory.createLabel();
        label = this.testPersistenceManager.save(label);
        this.mockMvc.perform(delete("/api/labels/{id}", label.getId())
                        .with(this.token))
                .andExpect(status().isNoContent());
        assertThat(this.labelRepository.existsById(label.getId())).isFalse();
    }

    @Test
    public void testDeleteWithTask() throws Exception {
        User user = this.testModelFactory.createUser();
        user = this.testPersistenceManager.save(user);
        TaskStatus taskStatus = this.testModelFactory.createTaskStatus();
        taskStatus = this.testPersistenceManager.save(taskStatus);
        Label label = this.testModelFactory.createLabel();
        label = this.testPersistenceManager.save(label);
        Task task = this.testModelFactory.createTask(user, taskStatus, label);
        this.testPersistenceManager.save(task);
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
