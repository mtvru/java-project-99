package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public final class TaskStatusControllerTest extends AbstractControllerTest {
    private final WebApplicationContext wac;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final TestPersistenceManager testPersistenceManager;
    private final TestModelFactory testModelFactory;
    private final ObjectMapper om;
    private final TaskStatusMapper mapper;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private TaskStatus testStatus;
    private User testUser;

    @Autowired
    public TaskStatusControllerTest(
            WebApplicationContext wac, TaskStatusRepository taskStatusRepository,
            TaskRepository taskRepository, ObjectMapper om, TestPersistenceManager testPersistenceManager,
            TestModelFactory testModelFactory, TaskStatusMapper mapper
    ) {
        this.wac = wac;
        this.taskStatusRepository = taskStatusRepository;
        this.taskRepository = taskRepository;
        this.om = om;
        this.testPersistenceManager = testPersistenceManager;
        this.testModelFactory = testModelFactory;
        this.mapper = mapper;
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
        this.testStatus = this.testModelFactory.createTaskStatus();
        this.testStatus = this.testPersistenceManager.save(this.testStatus);
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        List<TaskStatusDTO> dtos = this.om.readValue(body, new TypeReference<>() { });
        List<TaskStatus> actual = dtos.stream().map(this.mapper::map).toList();
        List<TaskStatus> expected = this.taskStatusRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(actual.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT)))
            .isEqualTo(
                    expected.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
            );
    }

    @Test
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/task_statuses/{id}", this.testStatus.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(this.testStatus.getName()),
                v -> v.node("slug").isEqualTo(this.testStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(
                        this.testStatus.getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
                )
        );
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, String> data = Map.of(
                "name", "NewStatus",
                "slug", "new_slug"
        );

        this.mockMvc.perform(post("/api/task_statuses")
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        TaskStatus status = this.taskStatusRepository.findBySlug("new_slug").get();
        assertThat(status.getName()).isEqualTo("NewStatus");
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, String> data = Map.of("name", "UpdatedName");

        this.mockMvc.perform(put("/api/task_statuses/{id}", this.testStatus.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        TaskStatus status = this.taskStatusRepository.findById(this.testStatus.getId()).get();
        assertThat(status.getName()).isEqualTo("UpdatedName");
        assertThat(status.getSlug()).isEqualTo(this.testStatus.getSlug());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        Map<String, String> data = Map.of("slug", "updated_slug");

        this.mockMvc.perform(put("/api/task_statuses/{id}", this.testStatus.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        TaskStatus status = this.taskStatusRepository.findById(this.testStatus.getId()).get();
        assertThat(status.getName()).isEqualTo(this.testStatus.getName());
        assertThat(status.getSlug()).isEqualTo("updated_slug");
    }

    @Test
    public void testDelete() throws Exception {
        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId())
                        .with(this.token))
                .andExpect(status().isNoContent());

        assertThat(this.taskStatusRepository.existsById(this.testStatus.getId())).isFalse();
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        Map<String, String> data = Map.of("name", "New", "slug", "new");
        this.mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        Map<String, String> data = Map.of("name", "Updated");
        this.mockMvc.perform(put("/api/task_statuses/{id}", this.testStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteWithTask() throws Exception {
        Task task = new Task();
        task.setName("task");
        task.setStatus(this.testStatus);
        this.taskRepository.save(task);

        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId())
                        .with(this.token))
                .andExpect(status().isConflict());

        assertThat(this.taskStatusRepository.existsById(this.testStatus.getId())).isTrue();
    }
}
