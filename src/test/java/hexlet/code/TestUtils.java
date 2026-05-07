package hexlet.code;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {
    private static final int NAME_SUFFIX_LENGTH = 5;

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    public TestUtils(
            LabelRepository labelRepository,
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            UserRepository userRepository,
            Faker faker,
            PasswordEncoder passwordEncoder
    ) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.faker = faker;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Clears all repositories, deleting all records from the database.
     */
    public void clear() {
        this.taskRepository.deleteAll();
        this.labelRepository.deleteAll();
        this.taskStatusRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    /**
     * Creates and saves a new user with random data.
     *
     * @return the saved user
     */
    public User createUser() {
        User user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> this.faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> this.passwordEncoder.encode("password"))
                .create();
        this.userRepository.save(user);
        return user;
    }

    /**
     * Creates and saves a new task status with a random name and slug.
     *
     * @return the saved task status
     */
    public TaskStatus createTaskStatus() {
        TaskStatus status = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> this.faker.lorem().word()
                        + this.faker.number().digits(NAME_SUFFIX_LENGTH))
                .supply(Select.field(TaskStatus::getSlug), () -> this.faker.internet().slug()
                        + this.faker.number().digits(NAME_SUFFIX_LENGTH))
                .create();
        this.taskStatusRepository.save(status);
        return status;
    }

    /**
     * Creates and saves a new label with a random name.
     *
     * @return the saved label
     */
    public Label createLabel() {
        Label label = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getTasks))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> this.faker.lorem().word()
                        + this.faker.number().digits(NAME_SUFFIX_LENGTH))
                .create();
        this.labelRepository.save(label);
        return label;
    }

    /**
     * Creates and saves a new task with the specified parameters.
     *
     * @param name the name of the task
     * @param assignee the user to whom the task is assigned
     * @param status   the task status
     * @param label    for the task
     * @return the saved task
     */
    public Task createTask(String name, User assignee, TaskStatus status, Label label) {
        Task task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getLabels))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> name)
                .supply(Select.field(Task::getDescription), () -> this.faker.lorem().paragraph())
                .supply(Select.field(Task::getTaskStatus), () -> status)
                .supply(Select.field(Task::getAssignee), () -> assignee)
                .create();
        task.addLabel(label);
        this.taskRepository.save(task);
        return task;
    }

    /**
     * Creates and saves a new task with a randomly generated name and the specified parameters.
     *
     * @param assignee the user to whom the task is assigned
     * @param status the task status
     * @param label  for the task
     * @return the saved task
     */
    public Task createTask(User assignee, TaskStatus status, Label label) {
        return createTask(this.faker.country().name(), assignee, status, label);
    }

    /**
     * Creates a full set of related entities for testing labels:
     * status, label, user, and a task linked to them.
     *
     * @return the created and saved label
     */
    public Label createLabelWithUserAndTaskAndTaskStatus() {
        TaskStatus status = createTaskStatus();
        Label label = createLabel();
        User user = createUser();
        this.createTask(user, status, label);
        return label;
    }
}
