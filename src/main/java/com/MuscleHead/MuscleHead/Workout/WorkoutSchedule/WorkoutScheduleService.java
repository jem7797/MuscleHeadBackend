package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Medal.MedalService;
import com.MuscleHead.MuscleHead.User.User;

import jakarta.transaction.Transactional;

@Service
public class WorkoutScheduleService {

    @Autowired
    private WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    private MedalService medalService;

    @Transactional
    public WorkoutSchedule create(User user, WorkoutScheduleRequest request) {
        WorkoutSchedule schedule = new WorkoutSchedule();
        schedule.setUser(user);
        schedule.setDay_of_the_week(request.getDay_of_the_week());
        schedule.setLabel(request.getLabel() != null ? request.getLabel() : "");
        WorkoutSchedule saved = workoutScheduleRepository.save(schedule);
        medalService.checkScheduleMedals(user);
        return saved;
    }

    @Transactional
    public Optional<WorkoutSchedule> patch(Long id, String userSubId, WorkoutSchedulePatchRequest request) {
        return workoutScheduleRepository.findById(id)
                .filter(ws -> ws.getUser().getSub_id().equals(userSubId))
                .map(ws -> {
                    if (request.getDay_of_the_week() != null) {
                        ws.setDay_of_the_week(request.getDay_of_the_week());
                    }
                    if (request.getLabel() != null) {
                        ws.setLabel(request.getLabel());
                    }
                    WorkoutSchedule saved = workoutScheduleRepository.save(ws);
                    medalService.checkScheduleMedals(saved.getUser());
                    return saved;
                });
    }

    public List<WorkoutSchedule> findByUserSubId(String userSubId) {
        return workoutScheduleRepository.findByUserSubId(userSubId);
    }
}
