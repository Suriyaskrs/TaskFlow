package com.taskflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project entity — maps to the 'projects' table.
 *
 * A project is a container for tasks and belongs to one user.
 *
 * Relationships:
 *   Project (Many) → (1) User
 *   Project (1)    → (Many) Tasks
 *
 * CascadeType.ALL on tasks: deleting a project deletes all its tasks.
 * orphanRemoval = true: tasks removed from the list are deleted from DB.
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Many projects → one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude   // avoid circular toString with Lombok
    @EqualsAndHashCode.Exclude
    private User user;

    // One project → many tasks
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Task> tasks = new ArrayList<>();
}