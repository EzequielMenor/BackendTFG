package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exercises")
public class Exercise {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "muscle_group", length = 50)
  private String muscleGroup;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @Column(length = 50)
  private String equipment;

  @Column(name = "secondary_muscles", columnDefinition = "TEXT")
  private String secondaryMuscles;

  @Column(name = "video_url", columnDefinition = "TEXT")
  private String videoUrl;

  @Column(name = "thumbnail_url", columnDefinition = "TEXT")
  private String thumbnailUrl;

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  public Exercise() {}

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getMuscleGroup() { return muscleGroup; }
  public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

  public String getEquipment() { return equipment; }
  public void setEquipment(String equipment) { this.equipment = equipment; }

  public String getSecondaryMuscles() { return secondaryMuscles; }
  public void setSecondaryMuscles(String secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

  public String getVideoUrl() { return videoUrl; }
  public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

  public String getThumbnailUrl() { return thumbnailUrl; }
  public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
