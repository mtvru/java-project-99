package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskParamsDTO extends IndexDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
