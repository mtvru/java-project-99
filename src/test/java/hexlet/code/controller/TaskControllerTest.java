package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.TestUtils;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class TaskControllerTest {
    private static final int TEST_INDEX = 10;

    private final WebApplicationContext wac;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final ObjectMapper om;
    private final TestUtils testUtils;
    private MockMvc mockMvc;
    private JwtRequestPostProcessor token;
    private Task testTask;
    private User testUser;
    private TaskStatus testStatus;

    @Autowired
    public TaskControllerTest(
        WebApplicationContext wac, TaskRepository taskRepository, TaskStatusRepository taskStatusRepository,
        UserRepository userRepository, ObjectMapper om, TestUtils testUtils
    ) {
        this.wac = wac;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.om = om;
        this.testUtils = testUtils;
    }

    /**
     * Creates a test user, task, and task status before each test.
     */
    @BeforeEach
    public void setUp() {
        this.testUtils.clear();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        this.testUser = this.testUtils.createUser();
        this.token = jwt().jwt(builder -> builder.subject(this.testUser.getEmail()));
        this.testStatus = this.testUtils.createTaskStatus();
        Label label = this.testUtils.createLabel();
        this.testTask = this.testUtils.createTask(this.testUser, this.testStatus, label);
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/tasks")
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).node("[0].createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    @Test
    public void testIndexWithFilter() throws Exception {
        Label label = this.testUtils.createLabel();
        this.testUtils.createTask("Specific task", this.testUser, this.testStatus, label);

        // Filter by title
        MvcResult result1 = mockMvc.perform(get("/api/tasks?titleCont=Specific")
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result1.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by assignee
        MvcResult result2 = mockMvc.perform(get("/api/tasks?assigneeId=" + testUser.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result2.getResponse().getContentAsString()).isArray().hasSize(2);

        // Filter by status
        MvcResult result3 = mockMvc.perform(get("/api/tasks?status=" + testStatus.getSlug())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result3.getResponse().getContentAsString()).isArray().hasSize(2);

        // Filter by label
        MvcResult result4 = mockMvc.perform(get("/api/tasks?labelId=" + label.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result4.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by multiple params
        MvcResult result5 = mockMvc.perform(get("/api/tasks?titleCont=Specific&labelId=" + label.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result5.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by multiple params - no results
        MvcResult result6 = mockMvc.perform(get("/api/tasks?titleCont=NonExistent&labelId=" + label.getId())
                        .with(this.token))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result6.getResponse().getContentAsString()).isArray().hasSize(0);
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
                v -> v.node("createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$")
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

        Task task = this.taskRepository.findAll().stream()
                .filter(t -> t.getName().equals("New Task"))
                .findFirst()
                .get();
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
        TaskStatus newStatus = this.testUtils.createTaskStatus();

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

    @Transactional
    @Test
    public void testCreateWithLabels() throws Exception {
        Label label1 = this.testUtils.createLabel();
        Label label2 = this.testUtils.createLabel();

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Task with labels");
        data.put("status", this.testStatus.getSlug());
        data.put("taskLabelIds", Set.of(label1.getId(), label2.getId()));

        this.mockMvc.perform(post("/api/tasks")
                        .with(this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Task task = this.taskRepository.findAll().stream()
                .filter(t -> t.getName().equals("Task with labels"))
                .findFirst()
                .get();

        assertThat(task.getLabels()).hasSize(2);
        assertThat(task.getLabels()).extracting(Label::getId).containsExactlyInAnyOrder(label1.getId(), label2.getId());
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
