package hexlet.code.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class WelcomeController {
    /**
     * Welcome page.
     * @return welcome message
     */
    @GetMapping("/welcome")
    public String index() {
        return "Welcome to Spring";
    }
}
