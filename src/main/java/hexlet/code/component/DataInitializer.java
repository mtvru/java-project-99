package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.enums.LabelName;
import hexlet.code.model.enums.TaskStatusName;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        TaskStatusRepository taskStatusRepository,
        LabelRepository labelRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.labelRepository = labelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        User userData = new User();
        if (this.userRepository.findByEmail("hexlet@example.com").isEmpty()) {
            userData.setEmail("hexlet@example.com");
            userData.setPassword(this.passwordEncoder.encode("qwerty"));
            this.userRepository.save(userData);
        }

        for (TaskStatusName statusName : TaskStatusName.values()) {
            if (this.taskStatusRepository.findBySlug(statusName.getSlug()).isEmpty()) {
                TaskStatus status = new TaskStatus();
                status.setName(statusName.getName());
                status.setSlug(statusName.getSlug());
                this.taskStatusRepository.save(status);
            }
        }

        for (LabelName labelName : LabelName.values()) {
            if (this.labelRepository.findByName(labelName.getName()).isEmpty()) {
                Label label = new Label();
                label.setName(labelName.getName());
                this.labelRepository.save(label);
            }
        }
    }
}
