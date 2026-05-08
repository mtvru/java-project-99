package hexlet.code;

import net.datafaker.Faker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {
    /**
     * Faker bean.
     * @return Faker instance
     */
    @Bean
    public Faker faker() {
        return new Faker();
    }
}
