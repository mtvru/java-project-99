package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndexDTO {
    @JsonProperty("_start")
    private Integer start;

    @JsonProperty("_end")
    private Integer end;

    @JsonProperty("_sort")
    private String sort;

    @JsonProperty("_order")
    private String order;
}
