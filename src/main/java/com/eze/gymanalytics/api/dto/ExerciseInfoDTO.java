package com.eze.gymanalytics.api.dto;

// Nuevo DTO (o modifícalo si ya lo tienes como record en Java 21)
public class ExerciseInfoDTO {
   private String name;
   private String muscleGroup;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getMuscleGroup() {
      return muscleGroup;
   }

   public void setMuscleGroup(String muscleGroup) {
      this.muscleGroup = muscleGroup;
   }
}