package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
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

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public final class TaskStatusControllerTest {
    private static final int DIGITS_COUNT = 5;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private Faker faker;
    private TaskStatus testStatus;

    @BeforeEach
    public void setUp() {
        this.taskRepository.deleteAll();
        this.taskStatusRepository.deleteAll();
        this.testStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName),
                        () -> this.faker.lorem().word() + this.faker.number().digits(DIGITS_COUNT))
                .supply(Select.field(TaskStatus::getSlug),
                        () -> this.faker.internet().slug() + this.faker.number().digits(DIGITS_COUNT))
                .create();
        this.taskStatusRepository.save(this.testStatus);
    }

    @Test
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(1);
        assertThatJson(body).node("[0].createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$");
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
                v -> v.node("createdAt").asString().matches("^\\d{4}-\\d{2}-\\d{2}$")
        );
    }

    @Test
    @WithMockUser
    public void testCreate() throws Exception {
        Map<String, String> data = Map.of(
                "name", "NewStatus",
                "slug", "new_slug"
        );

        this.mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        TaskStatus status = this.taskStatusRepository.findBySlug("new_slug").get();
        assertThat(status.getName()).isEqualTo("NewStatus");
    }

    @Test
    @WithMockUser
    public void testUpdate() throws Exception {
        Map<String, String> data = Map.of("name", "UpdatedName");

        this.mockMvc.perform(put("/api/task_statuses/{id}", this.testStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        TaskStatus status = this.taskStatusRepository.findById(this.testStatus.getId()).get();
        assertThat(status.getName()).isEqualTo("UpdatedName");
        assertThat(status.getSlug()).isEqualTo(this.testStatus.getSlug());
    }

    @Test
    @WithMockUser
    public void testPartialUpdate() throws Exception {
        Map<String, String> data = Map.of("slug", "updated_slug");

        this.mockMvc.perform(put("/api/task_statuses/{id}", this.testStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        TaskStatus status = this.taskStatusRepository.findById(this.testStatus.getId()).get();
        assertThat(status.getName()).isEqualTo(this.testStatus.getName());
        assertThat(status.getSlug()).isEqualTo("updated_slug");
    }

    @Test
    @WithMockUser
    public void testDelete() throws Exception {
        this.mockMvc.perform(delete("/api/task_statuses/{id}", this.testStatus.getId()))
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
}
