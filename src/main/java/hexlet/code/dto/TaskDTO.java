package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    private Long id;
    private Integer index;
    @JsonFormat(pattern = ISO_DATE_FORMAT)
    private LocalDate createdAt;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    @JsonProperty("title")
    private String name;
    @JsonProperty("content")
    private String description;
    @JsonProperty("status")
    private String taskStatus;
    private Set<Long> taskLabelIds;
}
