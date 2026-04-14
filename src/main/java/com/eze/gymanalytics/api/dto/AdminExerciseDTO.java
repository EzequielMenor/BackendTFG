package com.eze.gymanalytics.api.dto;

import java.util.List;

/**
 * DTO para la vista de administración de ejercicios.
 * Expone todos los campos del catálogo de ejercicios.
 */
public class AdminExerciseDTO {

    private Long id;
    private String name;
    private String muscleGroup;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private String equipment;
    private String secondaryMuscles;
    private List<String> aliases;
    private String createdAt;

    public AdminExerciseDTO() {}

    public AdminExerciseDTO(
            Long id,
            String name,
            String muscleGroup,
            String description,
            String imageUrl,
            String videoUrl,
            String thumbnailUrl,
            String equipment,
            String secondaryMuscles,
            List<String> aliases,
            String createdAt) {
        this.id = id;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.equipment = equipment;
        this.secondaryMuscles = secondaryMuscles;
        this.aliases = aliases;
        this.createdAt = createdAt;
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

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(String secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
