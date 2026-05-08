package hexlet.code;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TestPersistenceManager {
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    public TestPersistenceManager(
            LabelRepository labelRepository,
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            UserRepository userRepository
    ) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
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
     * Saves user entity.
     *
     * @param user user entity
     * @return saved user
     */
    public User save(User user) {
        return this.userRepository.save(user);
    }

    /**
     * Saves task status entity.
     *
     * @param status task status entity
     * @return saved task status
     */
    public TaskStatus save(TaskStatus status) {
        return this.taskStatusRepository.save(status);
    }

    /**
     * Saves label entity.
     *
     * @param label label entity
     * @return saved label
     */
    public Label save(Label label) {
        return this.labelRepository.save(label);
    }

    /**
     * Saves task entity.
     *
     * @param task task entity
     * @return saved a task
     */
    public Task save(Task task) {
        return this.taskRepository.save(task);
    }
}
