package hexlet.code.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class TaskStatusUpdateDTO {
    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> name = JsonNullable.undefined();

    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> slug = JsonNullable.undefined();
}
