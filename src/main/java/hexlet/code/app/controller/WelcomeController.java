package hexlet.code.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
