package hexlet.code.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
    /**
     * Configures the Jackson2ObjectMapperBuilder bean.
     * Sets the non-null inclusion policy and installs JsonNullableModule.
     * @return Jackson2ObjectMapperBuilder instance.
     */
    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
            .modulesToInstall(new JsonNullableModule());
        return builder;
    }
}
