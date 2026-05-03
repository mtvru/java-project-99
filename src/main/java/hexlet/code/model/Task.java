package hexlet.code.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Setter
@Getter
public class Task implements BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank
    @Size(min = 1)
    private String name;
    private Integer index;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(optional = false)
    @JoinColumn(name = "status_id")
    private TaskStatus taskStatus;
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;
    @CreatedDate
    private LocalDate createdAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = jakarta.persistence.FetchType.EAGER)
    private Set<TaskLabel> taskLabels = new HashSet<>();

    /**
     * Get labels.
     * @return set of labels
     */
    public final Set<Label> getLabels() {
        return taskLabels.stream()
                .map(TaskLabel::getLabel)
                .collect(Collectors.toSet());
    }

    /**
     * Set labels.
     * @param labels set of labels
     */
    public final void setLabels(Set<Label> labels) {
        if (labels == null) {
            this.taskLabels.clear();
            return;
        }

        Set<Label> currentLabels = getLabels();

        // Remove labels not in the new set
        this.taskLabels.removeIf(taskLabel -> !labels.contains(taskLabel.getLabel()));

        // Add new labels
        labels.forEach(label -> {
            if (!currentLabels.contains(label)) {
                this.taskLabels.add(new TaskLabel(this, label));
            }
        });
    }
}
