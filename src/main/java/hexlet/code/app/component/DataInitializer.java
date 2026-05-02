package hexlet.code.app.component;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.enums.LabelName;
import hexlet.code.app.model.enums.TaskStatusName;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public final class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String email = "hexlet@example.com";
        User userData = new User();

        if (this.userRepository.findByEmail(email).isEmpty()) {
            userData.setEmail(email);
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
