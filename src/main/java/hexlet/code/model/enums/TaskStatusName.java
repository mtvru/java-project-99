package hexlet.code.model.enums;

import lombok.Getter;

@Getter
public enum TaskStatusName {
    DRAFT("Draft", "draft"),
    TO_REVIEW("ToReview", "to_review"),
    TO_BE_FIXED("ToBeFixed", "to_be_fixed"),
    TO_PUBLISH("ToPublish", "to_publish"),
    PUBLISHED("Published", "published");

    private final String name;
    private final String slug;

    TaskStatusName(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
