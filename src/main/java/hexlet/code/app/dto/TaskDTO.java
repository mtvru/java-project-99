package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    private LocalDate createdAt;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    @JsonProperty("title")
    private String name;
    @JsonProperty("content")
    private String description;
    @JsonProperty("status")
    private String taskStatus;
}
