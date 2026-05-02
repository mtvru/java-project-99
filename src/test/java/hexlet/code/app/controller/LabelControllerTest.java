package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
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
public final class LabelControllerTest {
    private static final int NAME_SUFFIX_LENGTH = 5;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private Faker faker;
    private Label testLabel;

    @BeforeEach
    public void setUp() {
        this.taskRepository.deleteAll();
        this.taskStatusRepository.deleteAll();
        this.labelRepository.deleteAll();
        this.testLabel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .ignore(Select.field(Label::getTaskLabels))
                .supply(Select.field(Label::getName), () -> this.faker.lorem().word()
                        + this.faker.number().digits(NAME_SUFFIX_LENGTH))
                .create();
        this.labelRepository.save(this.testLabel);
    }

    @Test
    @WithMockUser
    public void testIndex() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(1);
    }

    @Test
    @WithMockUser
    public void testShow() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/labels/{id}", this.testLabel.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThatJson(body).node("name").isEqualTo(this.testLabel.getName());
    }

    @Test
    @WithMockUser
    public void testCreate() throws Exception {
        Map<String, String> data = Map.of("name", "New Label");

        this.mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isCreated());

        Label label = this.labelRepository.findByName("New Label").get();
        assertThat(label.getName()).isEqualTo("New Label");
    }

    @Test
    @WithMockUser
    public void testUpdate() throws Exception {
        Map<String, String> data = Map.of("name", "Updated Label");

        this.mockMvc.perform(put("/api/labels/{id}", this.testLabel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.om.writeValueAsString(data)))
                .andExpect(status().isOk());

        Label label = this.labelRepository.findById(this.testLabel.getId()).get();
        assertThat(label.getName()).isEqualTo("Updated Label");
    }

    @Test
    @WithMockUser
    public void testDelete() throws Exception {
        this.mockMvc.perform(delete("/api/labels/{id}", this.testLabel.getId()))
                .andExpect(status().isNoContent());

        assertThat(this.labelRepository.existsById(this.testLabel.getId())).isFalse();
    }

    @Test
    @WithMockUser
    public void testDeleteWithTask() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("status");
        status.setSlug("status");
        this.taskStatusRepository.save(status);

        Task task = new Task();
        task.setName("task");
        task.setTaskStatus(status);
        task.setLabels(java.util.Set.of(this.testLabel));
        this.taskRepository.save(task);

        this.mockMvc.perform(delete("/api/labels/{id}", this.testLabel.getId()))
                .andExpect(status().isInternalServerError());

        assertThat(this.labelRepository.existsById(this.testLabel.getId())).isTrue();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }
}
