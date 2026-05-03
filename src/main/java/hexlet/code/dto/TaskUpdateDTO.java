package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<Integer> index;
    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;
    @NotBlank
    @JsonProperty("title")
    private JsonNullable<String> name;
    @JsonProperty("content")
    private JsonNullable<String> description;
    @NotBlank
    @JsonProperty("status")
    private JsonNullable<String> taskStatus;
    private JsonNullable<Set<Long>> taskLabelIds;
}
