package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String email = "hexlet@example.com";
        User userData = new User();

        if (userRepository.findByEmail(email).isEmpty()) {
            userData.setEmail(email);
            userData.setPassword(passwordEncoder.encode("qwerty"));
            userRepository.save(userData);
        }
    }
}