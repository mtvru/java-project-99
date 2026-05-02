package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public final class TaskControllerTest {
    private static final int DIGITS_COUNT = 3;
    private static final int TEST_INDEX = 100;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private Faker faker;
    private Task testTask;
    private User testUser;
    private TaskStatus testStatus;

    @BeforeEach
    public void setUp() {
        this.taskRepository.deleteAll();
        this.taskStatusRepository.deleteAll();
        this.userRepository.deleteAll();
        this.labelRepository.deleteAll();
        this.testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> this.faker.internet().password())
                .create();
        this.userRepository.save(this.testUser);
        this.testStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName),
                        () -> this.faker.lorem().word() + this.faker.number().digits(DIGITS_COUNT))
                .supply(Select.field(TaskStatus::getSlug),
                        () -> this.faker.internet().slug() + this.faker.number().digits(DIGITS_COUNT))
                .create();
        this.taskStatusRepository.save(this.testStatus);
        this.testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getTaskLabels))
                .supply(Select.field(Task::getName), () -> this.faker.lorem().sentence())
                .supply(Select.field(Task::getDescription), () -> this.faker.lorem().paragraph())
                .supply(Select.field(Task::getTaskStatus), () -> this.testStatus)
                .supply(Select.field(Task::getAssignee), () -> this.testUser)
                .create();
        this.taskRepository.save(this.testTask);
    }

    @Test
    @WithMockUser
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).node("[0].createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    @Test
    @WithMockUser
    public void testIndexWithFilter() throws Exception {
        Label label = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getTaskLabels))
                .supply(Select.field(Label::getName), () -> faker.lorem().word() + faker.number().digits(DIGITS_COUNT))
                .create();
        labelRepository.save(label);

        Task task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getTaskLabels))
                .supply(Select.field(Task::getName), () -> "Specific title")
                .supply(Select.field(Task::getTaskStatus), () -> testStatus)
                .supply(Select.field(Task::getAssignee), () -> testUser)
                .create();
        taskRepository.save(task);

        task.setLabels(Set.of(label));
        taskRepository.save(task);

        // Filter by title
        MvcResult result1 = mockMvc.perform(get("/api/tasks?titleCont=Specific"))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result1.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by assignee
        MvcResult result2 = mockMvc.perform(get("/api/tasks?assigneeId=" + testUser.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result2.getResponse().getContentAsString()).isArray().hasSize(2);

        // Filter by status
        MvcResult result3 = mockMvc.perform(get("/api/tasks?status=" + testStatus.getSlug()))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result3.getResponse().getContentAsString()).isArray().hasSize(2);

        // Filter by label
        MvcResult result4 = mockMvc.perform(get("/api/tasks?labelId=" + label.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result4.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by multiple params
        MvcResult result5 = mockMvc.perform(get("/api/tasks?titleCont=Specific&labelId=" + label.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result5.getResponse().getContentAsString()).isArray().hasSize(1);

        // Filter by multiple params - no results
        MvcResult result6 = mockMvc.perform(get("/api/tasks?titleCont=NonExistent&labelId=" + label.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThatJson(result6.getResponse().getContentAsString()).isArray().hasSize(0);
    }

    @Test
    @WithMockUser
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/tasks/{id}", this.testTask.getId()))
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
    @WithMockUser
    public void testCreate() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Task");
        data.put("content", "New Description");
        data.put("status", this.testStatus.getSlug());
        data.put("assignee_id", this.testUser.getId());
        data.put("index", TEST_INDEX);
        this.mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Task task = this.taskRepository.findAll().stream()
                .filter(t -> t.getName().equals("New Task"))
                .findFirst()
                .get();
        assertThat(task.getDescription()).isEqualTo("New Description");
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(this.testStatus.getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(this.testUser.getId());
        assertThat(task.getIndex()).isEqualTo(TEST_INDEX);
    }

    @Test
    @WithMockUser
    public void testUpdate() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Task");
        data.put("content", "Updated Description");
        this.mockMvc.perform(put("/api/tasks/{id}", this.testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());
        Task task = this.taskRepository.findById(this.testTask.getId()).get();
        assertThat(task.getName()).isEqualTo("Updated Task");
        assertThat(task.getDescription()).isEqualTo("Updated Description");
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(this.testStatus.getSlug());
    }

    @Test
    @WithMockUser
    public void testPartialUpdateStatus() throws Exception {
        TaskStatus newStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName),
                        () -> this.faker.lorem().word() + this.faker.number().digits(DIGITS_COUNT))
                .supply(Select.field(TaskStatus::getSlug),
                        () -> this.faker.internet().slug() + this.faker.number().digits(DIGITS_COUNT))
                .create();
        this.taskStatusRepository.save(newStatus);

        Map<String, Object> data = Map.of("status", newStatus.getSlug());

        this.mockMvc.perform(put("/api/tasks/{id}", this.testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        Task task = this.taskRepository.findById(this.testTask.getId()).get();
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(newStatus.getSlug());
    }

    @Test
    @WithMockUser
    public void testDelete() throws Exception {
        this.mockMvc.perform(delete("/api/tasks/{id}", this.testTask.getId()))
                .andExpect(status().isNoContent());

        assertThat(this.taskRepository.existsById(this.testTask.getId())).isFalse();
    }

    @Test
    @WithMockUser
    public void testDeleteUserWithTask() throws Exception {
        this.mockMvc.perform(delete("/api/users/{id}", this.testUser.getId()))
                .andExpect(status().isInternalServerError());

        assertThat(this.userRepository.existsById(this.testUser.getId())).isTrue();
    }

    @Test
    @WithMockUser
    public void testDeleteStatusWithTask() throws Exception {
        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId()))
                .andExpect(status().isInternalServerError());

        assertThat(this.taskStatusRepository.existsById(this.testStatus.getId())).isTrue();
    }

    @Test
    @WithMockUser
    public void testCreateWithLabels() throws Exception {
        Label label1 = new Label();
        label1.setName("label1");
        this.labelRepository.save(label1);

        Label label2 = new Label();
        label2.setName("label2");
        this.labelRepository.save(label2);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Task with labels");
        data.put("status", this.testStatus.getSlug());
        data.put("taskLabelIds", Set.of(label1.getId(), label2.getId()));

        this.mockMvc.perform(post("/api/tasks")
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
