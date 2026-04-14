package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "exercises")
public class Exercise {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100, unique = true)
  private String name;

  @Column(name = "muscle_group", length = 50)
  private String muscleGroup;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  // ── Campos añadidos para sincronizar con el esquema completo de la BD ──

  @Column(name = "equipment", length = 100)
  private String equipment;

  @Column(name = "secondary_muscles", columnDefinition = "TEXT")
  private String secondaryMuscles;

  @Column(name = "video_url", columnDefinition = "TEXT")
  private String videoUrl;

  @Column(name = "thumbnail_url", columnDefinition = "TEXT")
  private String thumbnailUrl;

  /**
   * aliases es un array de texto en PostgreSQL (text[]).
   * Se usa @JdbcTypeCode(SqlTypes.ARRAY) + @Array para que Hibernate lo trate
   * como array nativo de Postgres, no como JSON. Sin esto, la lectura falla con
   * "cannot cast type jsonb to text[]" y devuelve HTTP 500.
   */
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Array(length = 50)
  @Column(name = "aliases", columnDefinition = "text[]")
  private List<String> aliases;

  // ─────────────────────────────────────────────────────────────────────────

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

  public List<String> getAliases() { return aliases; }
  public void setAliases(List<String> aliases) { this.aliases = aliases; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
