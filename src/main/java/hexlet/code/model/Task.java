package hexlet.code.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

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
    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Label> labels = new HashSet<>();
    @NotBlank
    @Size(min = 1)
    private String name;
    private Integer index;
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private TaskStatus status;
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;
    @CreatedDate
    private LocalDate createdAt;

    /**
     * Add a label to the task.
     * @param label to add
     */
    public void addLabel(Label label) {
        this.labels.add(label);
        label.getTasks().add(this);
    }

    /**
     * Remove a label from the task.
     * @param label to remove
     */
    public void removeTag(Label label) {
        this.labels.remove(label);
        label.getTasks().remove(this);
    }
}
