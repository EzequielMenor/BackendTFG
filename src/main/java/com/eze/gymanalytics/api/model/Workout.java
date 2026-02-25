package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "workouts")
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

  public Workout() {}

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Profile getUser() { return user; }
  public void setUser(Profile user) { this.user = user; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public OffsetDateTime getStartTime() { return startTime; }
  public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

  public OffsetDateTime getEndTime() { return endTime; }
  public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public BigDecimal getTotalVolume() { return totalVolume; }
  public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

  public List<WorkoutExercise> getWorkoutExercises() { return workoutExercises; }
  public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) { this.workoutExercises = workoutExercises; }
}
