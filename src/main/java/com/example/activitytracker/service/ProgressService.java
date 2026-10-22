package com.example.activitytracker.service;

import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.model.TaskEntry;

import java.util.List;

public interface ProgressService {

    ProgressResponseDTO getProgress();

    ProgressResponseDTO calculateProgress(List<TaskEntry> tasks);
}