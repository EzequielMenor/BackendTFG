package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
  @Id
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(unique = true)
  private String username;

  @Column(name = "photo_url")
  private String photoUrl;

  @Column(length = 20)
  private String role = "user";

  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
