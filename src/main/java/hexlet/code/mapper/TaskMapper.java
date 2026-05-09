package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    uses = {JsonNullableMapper.class, ReferenceMapper.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "status", source = "taskStatus")
    @Mapping(target = "labels", source = "taskLabelIds")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "taskStatus", source = "status.slug")
    @Mapping(target = "taskLabelIds", source = "labels")
    public abstract TaskDTO map(Task model);

    @Mapping(source = "assigneeId", target = "assignee.id")
    @Mapping(source = "taskStatus", target = "status.slug")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract Task map(TaskDTO dto);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "status", source = "taskStatus")
    @Mapping(target = "labels", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @AfterMapping
    protected final void updateLabels(TaskUpdateDTO dto, @MappingTarget Task model) {
        if (dto.getTaskLabelIds() == null || !dto.getTaskLabelIds().isPresent()) {
            return;
        }
        model.getLabels().clear();
        Set<Long> labelIds = dto.getTaskLabelIds().get();
        if (labelIds == null || labelIds.isEmpty()) {
            return;
        }
        Set<Label> labels = toLabelSet(labelIds);
        if (labels.isEmpty()) {
            return;
        }
        model.getLabels().addAll(labels);
    }

    protected final TaskStatus toTaskStatus(String slug) {
        return this.taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("TaskStatus with slug " + slug + " not found"));
    }

    protected final Set<Long> toLabelIds(Set<Label> labels) {
        return labels == null ? null : labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }

    protected final Set<Label> toLabelSet(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(labelRepository.findAllById(labelIds));
    }
}
