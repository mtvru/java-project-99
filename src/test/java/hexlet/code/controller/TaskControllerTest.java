package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
import hexlet.code.dto.TaskDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
public final class TaskControllerTest extends AbstractControllerTest {
    private static final int TEST_INDEX = 10;
    private static final String TASK_NAME_SPECIFIC = "Specific";

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private TestPersistenceManager testPersistenceManager;
    @Autowired
    private TestModelFactory testModelFactory;
    @Autowired
    private TaskMapper mapper;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private Task testTask;
    private User testUser;
    private TaskStatus testStatus;
    private Label testLabel;

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
        this.testLabel = this.testModelFactory.createLabel();
        this.testLabel = this.testPersistenceManager.save(this.testLabel);
        this.testTask = this.testModelFactory.createTask(this.testUser, this.testStatus, this.testLabel);
        this.testTask = this.testPersistenceManager.save(this.testTask);

        Task task = this.testModelFactory.createTask(
                TASK_NAME_SPECIFIC + " task", this.testUser, this.testStatus, this.testLabel
        );
        this.testPersistenceManager.save(task);
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/tasks")
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        List<TaskDTO> dtos = this.om.readValue(body, new TypeReference<>() { });
        List<Task> actual = dtos.stream().map(this.mapper::map).toList();
        List<Task> expected = this.taskRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(actual.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT)))
            .isEqualTo(
                    expected.getFirst().getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
            );
    }

    @ParameterizedTest
    @MethodSource("filterParams")
    public void testIndexWithFilter(String query, int size) throws Exception {
        String queryString = query.replace("{assigneeId}", String.valueOf(testUser.getId()))
                .replace("{status}", testStatus.getSlug())
                .replace("{labelId}", String.valueOf(testLabel.getId()));

        MvcResult result = mockMvc.perform(get("/api/tasks?" + queryString)
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result.getResponse().getContentAsString()).isArray().hasSize(size);
    }

    private static Stream<Arguments> filterParams() {
        return Stream.of(
                Arguments.of("titleCont=" + TASK_NAME_SPECIFIC, 1),
                Arguments.of("assigneeId={assigneeId}", 2),
                Arguments.of("status={status}", 2),
                Arguments.of("labelId={labelId}", 2),
                Arguments.of("titleCont=" + TASK_NAME_SPECIFIC + "&labelId={labelId}", 1),
                Arguments.of("titleCont=NonExistent&labelId={labelId}", 0)
        );
    }

    @Test
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/tasks/{id}", this.testTask.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(this.testTask.getName()),
                v -> v.node("content").isEqualTo(this.testTask.getDescription()),
                v -> v.node("status").isEqualTo(this.testStatus.getSlug()),
                v -> v.node("assignee_id").isEqualTo(this.testUser.getId()),
                v -> v.node("createdAt").isEqualTo(
                        this.testTask.getCreatedAt().format(DateTimeFormatter.ofPattern(TaskDTO.ISO_DATE_FORMAT))
                )
        );
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Task");
        data.put("content", "New Description");
        data.put("status", this.testStatus.getSlug());
        data.put("assignee_id", this.testUser.getId());
        data.put("index", TEST_INDEX);
        this.mockMvc.perform(post("/api/tasks")
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Task task = this.taskRepository.findByName("New Task").get();
        assertThat(task.getDescription()).isEqualTo("New Description");
        assertThat(task.getStatus().getSlug()).isEqualTo(this.testStatus.getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(this.testUser.getId());
        assertThat(task.getIndex()).isEqualTo(TEST_INDEX);
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Task");
        data.put("content", "Updated Description");
        this.mockMvc.perform(put("/api/tasks/{id}", this.testTask.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());
        Task task = this.taskRepository.findById(this.testTask.getId()).get();
        assertThat(task.getName()).isEqualTo("Updated Task");
        assertThat(task.getDescription()).isEqualTo("Updated Description");
        assertThat(task.getStatus().getSlug()).isEqualTo(this.testStatus.getSlug());
    }

    @Test
    public void testPartialUpdateStatus() throws Exception {
        TaskStatus newStatus = this.testModelFactory.createTaskStatus();
        newStatus = this.testPersistenceManager.save(newStatus);

        Map<String, Object> data = Map.of("status", newStatus.getSlug());

        this.mockMvc.perform(put("/api/tasks/{id}", this.testTask.getId())
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        Task task = this.taskRepository.findById(this.testTask.getId()).get();
        assertThat(task.getStatus().getSlug()).isEqualTo(newStatus.getSlug());
    }

    @Test
    public void testDelete() throws Exception {
        this.mockMvc.perform(delete("/api/tasks/{id}", this.testTask.getId())
                        .with(this.token))
                .andExpect(status().isNoContent());

        assertThat(this.taskRepository.existsById(this.testTask.getId())).isFalse();
    }

    @Test
    public void testDeleteUserWithTask() throws Exception {
        this.mockMvc.perform(delete("/api/users/{id}", this.testUser.getId())
                        .with(this.token))
                .andExpect(status().isConflict());

        assertThat(this.userRepository.existsById(this.testUser.getId())).isTrue();
    }

    @Test
    public void testDeleteStatusWithTask() throws Exception {
        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId())
                        .with(this.token))
                .andExpect(status().isConflict());

        assertThat(this.taskStatusRepository.existsById(this.testStatus.getId())).isTrue();
    }

    @Test
    public void testCreateWithLabels() throws Exception {
        Label label1 = this.testModelFactory.createLabel();
        Label label2 = this.testModelFactory.createLabel();
        label1 = this.testPersistenceManager.save(label1);
        label2 = this.testPersistenceManager.save(label2);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Task with labels");
        data.put("status", this.testStatus.getSlug());
        data.put("taskLabelIds", Set.of(label1.getId(), label2.getId()));

        this.mockMvc.perform(post("/api/tasks")
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Task task = this.taskRepository.findByName("Task with labels").get();

        assertThat(task.getLabels()).hasSize(2);
        assertThat(task.getLabels()).extracting(Label::getId).containsExactlyInAnyOrder(label1.getId(), label2.getId());
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
