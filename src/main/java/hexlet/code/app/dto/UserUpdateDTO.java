package hexlet.code.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDTO {
    private JsonNullable<String> firstName = JsonNullable.undefined();
    private JsonNullable<String> lastName = JsonNullable.undefined();
    @NotBlank
    private JsonNullable<String> email = JsonNullable.undefined();
    private JsonNullable<String> password = JsonNullable.undefined();
}
