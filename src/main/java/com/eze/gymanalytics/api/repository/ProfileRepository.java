package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
  Optional<Profile> findByEmail(String email);
}
