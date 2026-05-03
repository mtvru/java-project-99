package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelCreateDTO {
    private static final int NAME_MIN_LENGTH = 3;
    private static final int NAME_MAX_LENGTH = 1000;

    @NotBlank
    @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
    private String name;
}
