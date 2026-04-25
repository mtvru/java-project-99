package hexlet.code.app.service;

import hexlet.code.app.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //TODO
    public void createUser() {
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }
}
