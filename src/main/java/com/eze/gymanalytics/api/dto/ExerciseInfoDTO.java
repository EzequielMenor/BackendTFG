package com.eze.gymanalytics.api.dto;

// Nuevo DTO (o modifícalo si ya lo tienes como record en Java 21)
public class ExerciseInfoDTO {
   private Long id;
   private String name;
   private String muscleGroup;
   private String thumbnailUrl;

   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public String getMuscleGroup() { return muscleGroup; }
   public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

   public String getThumbnailUrl() { return thumbnailUrl; }
   public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

   private String videoUrl;
   public String getVideoUrl() { return videoUrl; }
   public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}