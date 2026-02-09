package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "routines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Profile user;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_public")
  private Boolean isPublic = false;

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoutineExercise> routineExercises;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
