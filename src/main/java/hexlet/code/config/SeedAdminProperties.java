package hexlet.code.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "seed.admin")
public record SeedAdminProperties(
    @DefaultValue("hexlet@example.com") //due to hexlet check uses his own config
    String email,
    @DefaultValue("qwerty")  //due to hexlet check uses his own config
    String password
) {
}
