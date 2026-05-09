package hexlet.code;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;

@TestComponent
public final class TestModelFactory {
    private final AtomicInteger sequence = new AtomicInteger();
    private final PasswordEncoder passwordEncoder;
    private final Model<User> userModel;
    private final Model<TaskStatus> taskStatusModel;
    private final Model<Label> labelModel;
    private final Model<Task> taskModel;

    public TestModelFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        Settings settings = Settings.create()
                .set(Keys.FAIL_ON_ERROR, true);
        this.userModel = Instancio.of(User.class)
                .withSettings(settings)
                .ignore(Select.field(User::getId))
                .toModel();
        this.taskStatusModel = Instancio.of(TaskStatus.class)
                .withSettings(settings)
                .ignore(Select.field(TaskStatus::getId))
                .toModel();
        this.labelModel = Instancio.of(Label.class)
                .withSettings(settings)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getTasks))
                .ignore(Select.field(Label::getCreatedAt))
                .toModel();
        this.taskModel = Instancio.of(Task.class)
                .withSettings(settings)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getLabels))
                .ignore(Select.field(Task::getCreatedAt))
                .toModel();
    }

    public User createUser() {
        int seq = this.incSequence();
        return Instancio.of(userModel)
                .set(Select.field(User::getFirstName), "John-" + seq)
                .set(Select.field(User::getLastName), "Doe-" + seq)
                .set(Select.field(User::getEmail), "user-" + seq + "@example.com")
                .set(Select.field(User::getPassword), passwordEncoder.encode("password"))
                .create();
    }

    public User createUser(String email, String password) {
        return Instancio.of(User.class)
                .supply(Select.field(User::getEmail), () -> email)
                .supply(Select.field(User::getPassword), () -> password)
                .create();
    }

    public TaskStatus createTaskStatus() {
        int seq = this.incSequence();
        return Instancio.of(taskStatusModel)
                .set(Select.field(TaskStatus::getName), "status-" + seq)
                .set(Select.field(TaskStatus::getSlug), "status-" + seq)
                .create();
    }

    public Label createLabel() {
        int seq = this.incSequence();
        return Instancio.of(labelModel)
                .set(Select.field(Label::getName), "label-" + seq)
                .create();
    }

    public Task createTask(String name, User assignee, TaskStatus status, Label label) {
        int seq = this.incSequence();
        Task task = Instancio.of(taskModel)
                .set(Select.field(Task::getName), name)
                .set(
                        Select.field(Task::getDescription),
                        "description-" + seq
                )
                .set(Select.field(Task::getStatus), status)
                .set(Select.field(Task::getAssignee), assignee)
                .create();
        task.addLabel(label);
        return task;
    }

    public Task createTask(User assignee, TaskStatus status, Label label) {
        int seq = this.incSequence();
        return createTask("task-" + seq, assignee, status, label);
    }

    private int incSequence() {
        return this.sequence.incrementAndGet();
    }
}
