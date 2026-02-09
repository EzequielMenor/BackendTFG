package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Profile user;

  @Column(length = 100)
  private String name;

  @Column(name = "start_time", nullable = false)
  private OffsetDateTime startTime;

  @Column(name = "end_time")
  private OffsetDateTime endTime;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "total_volume")
  private BigDecimal totalVolume;

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkoutExercise> workoutExercises;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
