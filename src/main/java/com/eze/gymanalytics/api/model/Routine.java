package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "routines")
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

  @Builder.Default
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

  // Manual getters/setters to avoid relying on Lombok processing
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Profile getUser() {
    return user;
  }

  public void setUser(Profile user) {
    this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public List<RoutineExercise> getRoutineExercises() {
    return routineExercises;
  }

  public void setRoutineExercises(List<RoutineExercise> routineExercises) {
    this.routineExercises = routineExercises;
  }
}
