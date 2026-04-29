package hexlet.code.app.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserIndexDTO {
    private Integer _start;
    private Integer _end;
    private String _sort;
    private String _order;
}
