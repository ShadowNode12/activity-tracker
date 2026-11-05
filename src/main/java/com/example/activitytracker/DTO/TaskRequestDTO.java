package com.example.activitytracker.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class TaskRequestDTO {

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Task date cannot be in the future")
    private LocalDate date;

    @NotBlank(message = "Task Description is required")
    @Size(max = 500,
          message = "Task Description must not exceed 500 characters")
    private String taskDescription;

    private boolean completed;
}