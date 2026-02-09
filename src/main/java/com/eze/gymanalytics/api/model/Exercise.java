package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
