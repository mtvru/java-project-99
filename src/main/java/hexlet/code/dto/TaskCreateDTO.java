package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    @NotBlank
    @JsonProperty("title")
    private String name;
    @JsonProperty("content")
    private String description;
    @NotBlank
    @JsonProperty("status")
    private String taskStatus;
    private Set<Long> taskLabelIds;
}
