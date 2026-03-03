package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;

import jakarta.transaction.Transactional;

@Service
public class WorkoutScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutScheduleService.class);

    @Autowired
    private WorkoutScheduleRepository workoutScheduleRepository;

    @Transactional
    public WorkoutSchedule create(User user, WorkoutScheduleRequest request) {
        logger.debug("Creating workout schedule for user: {}", user.getSub_id());
        WorkoutSchedule schedule = new WorkoutSchedule();
        schedule.setUser(user);
        schedule.setDay_of_the_week(request.getDay_of_the_week());
        schedule.setLabel(request.getLabel() != null ? request.getLabel() : "");
        WorkoutSchedule saved = workoutScheduleRepository.save(schedule);
        logger.info("Created workout schedule id: {} for user: {}", saved.getId(), user.getSub_id());
        return saved;
    }

    @Transactional
    public Optional<WorkoutSchedule> patch(Long id, String userSubId, WorkoutSchedulePatchRequest request) {
        logger.debug("Patching workout schedule id: {} for user: {}", id, userSubId);
        return workoutScheduleRepository.findById(id)
                .filter(ws -> ws.getUser().getSub_id().equals(userSubId))
                .map(ws -> {
                    if (request.getDay_of_the_week() != null) {
                        ws.setDay_of_the_week(request.getDay_of_the_week());
                    }
                    if (request.getLabel() != null) {
                        ws.setLabel(request.getLabel());
                    }
                    return workoutScheduleRepository.save(ws);
                });
    }

    public List<WorkoutSchedule> findByUserSubId(String userSubId) {
        return workoutScheduleRepository.findByUserSubId(userSubId);
    }
}
