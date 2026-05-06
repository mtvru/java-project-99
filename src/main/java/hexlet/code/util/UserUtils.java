package hexlet.code.util;

import hexlet.code.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    private final UserRepository userRepository;

    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Check if the user is an owner of the resource.
     * @param id resource id
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String email = authentication.getName();
        return userRepository.findById(id)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }
}
