package hexlet.code.mapper;

import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

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
    private ReferenceMapper referenceMapper;

    @Autowired
    private QueryMapper queryMapper;

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "taskStatus")
    @Mapping(target = "labels", source = "taskLabelIds")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "taskStatus", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "taskStatus")
    @Mapping(target = "labels", source = "taskLabelIds")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    public final Pageable map(IndexDTO dto) {
        return queryMapper.toPageable(dto);
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
        return this.referenceMapper.toEntitySet(labelIds, Label.class);
    }
}
