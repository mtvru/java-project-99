package hexlet.code.app.model.enums;

import lombok.Getter;

@Getter
public enum LabelName {
    FEATURE("feature"),
    BUG("bug");

    private final String name;

    LabelName(String name) {
        this.name = name;
    }
}
