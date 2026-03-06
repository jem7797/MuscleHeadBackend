package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkedMusclesResponse {

    private List<String> frontWorked;
    private List<String> backWorked;
}
