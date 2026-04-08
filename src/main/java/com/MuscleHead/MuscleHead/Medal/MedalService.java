package com.MuscleHead.MuscleHead.Medal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Notification.NotificationService;
import com.MuscleHead.MuscleHead.Notification.NotificationType;
import com.MuscleHead.MuscleHead.Post.Comment.CommentRepository;
import com.MuscleHead.MuscleHead.Post.PostRepository;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstance;
import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstanceRepository;
import com.MuscleHead.MuscleHead.Workout.SessionLog.SessionLog;
import com.MuscleHead.MuscleHead.Workout.SessionLog.SessionLogRepository;

import jakarta.transaction.Transactional;

@Service
public class MedalService {

    private static final int GYM_RAT_HOURS = 24;
    private static final int SAME_TIME_DAYS = 5;
    private static final int STICK_FIGURE_CONSECUTIVE = 5;

    @Autowired
    private UserMedalRepository userMedalRepository;

    @Autowired
    private SessionLogRepository sessionLogRepository;

    @Autowired
    private SessionInstanceRepository sessionInstanceRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Checks and awards medals after a workout is posted.
     * @return list of newly awarded medals (for inclusion in the response)
     */
    @Transactional
    public List<MedalResponse> checkAndAwardMedals(User user, SessionLog justSavedSessionLog) {
        List<MedalResponse> newlyAwarded = new ArrayList<>();
        if (user == null || user.getSub_id() == null) return newlyAwarded;
        String subId = user.getSub_id();

        // BAPTISM - first workout
        if (sessionLogRepository.countByUser_SubId(subId) == 1) {
            tryAward(user, MedalName.BAPTISM, "First workout completed!", newlyAwarded);
        }

        // NO_PAIN_NO_GAIN - 5 workouts
        if (sessionLogRepository.countByUser_SubId(subId) >= 5) {
            tryAward(user, MedalName.NO_PAIN_NO_GAIN, "5 workouts completed!", newlyAwarded);
        }

        // CALL_ME_CLOTH_THE_WAY_I_STAY_IRONED - 10 workouts
        if (sessionLogRepository.countByUser_SubId(subId) >= 10) {
            tryAward(user, MedalName.CALL_ME_CLOTH_THE_WAY_I_STAY_IRONED, "10 workouts completed!", newlyAwarded);
        }

        // LIGHT_WEIGHT_BABY - first 225 lb lift
        if (user.getHighest_weight_lifted() >= 225) {
            tryAward(user, MedalName.LIGHT_WEIGHT_BABY, "First 225 lb lift!", newlyAwarded);
        }

        // VETERAN - 50 workouts
        if (sessionLogRepository.countByUser_SubId(subId) >= 50) {
            tryAward(user, MedalName.VETERAN, "50 workouts logged!", newlyAwarded);
        }

        // EVERYONE_WANTS_TO_BE_A_BODY_BUILDER - first 405 lbs
        if (user.getHighest_weight_lifted() >= 405) {
            tryAward(user, MedalName.EVERYONE_WANTS_TO_BE_A_BODY_BUILDER, "First 405 lb lift!", newlyAwarded);
        }

        // BUT_NOBODY_WANNA_LIFT_THIS_HEAVY_WEIGHT - first 500 lbs
        if (user.getHighest_weight_lifted() >= 500) {
            tryAward(user, MedalName.BUT_NOBODY_WANNA_LIFT_THIS_HEAVY_WEIGHT, "First 500 lb lift!", newlyAwarded);
        }

        // MENTZER_FLOW - workout under 30 minutes
        if (justSavedSessionLog.getTimeSpentInGym() > 0 && justSavedSessionLog.getTimeSpentInGym() < 1800) {
            tryAward(user, MedalName.MENTZER_FLOW, "Workout under 30 minutes!", newlyAwarded);
        }

        // FREQUENT_FLIER - 25 workouts
        if (sessionLogRepository.countByUser_SubId(subId) >= 25) {
            tryAward(user, MedalName.FREQUENT_FLIER, "25 workouts logged!", newlyAwarded);
        }

        // YEA_I_WORK_HERE - 100 workouts
        if (sessionLogRepository.countByUser_SubId(subId) >= 100) {
            tryAward(user, MedalName.YEA_I_WORK_HERE, "100 workouts logged!", newlyAwarded);
        }

        // GYM_RAT - total gym time exceeds 24 hours (86400 seconds)
        if (user.getLifetime_gym_time() >= GYM_RAT_HOURS * 3600) {
            tryAward(user, MedalName.GYM_RAT, "24+ hours in the gym!", newlyAwarded);
        }

        // ZHE_PUMP_IS_ZHE_BEST_FEELING - first chest movement (must be first time ever)
        if (sessionHasMovementArea(justSavedSessionLog, "chest") && isFirstWorkoutWithArea(subId, justSavedSessionLog.getId(), "chest")) {
            tryAward(user, MedalName.ZHE_PUMP_IS_ZHE_BEST_FEELING, "Completed a chest movement!", newlyAwarded);
        }

        // STICK_FIGURE - 5 consecutive workouts without legs
        if (hasConsecutiveWorkoutsWithoutLegs(subId, STICK_FIGURE_CONSECUTIVE)) {
            tryAward(user, MedalName.STICK_FIGURE, "5 consecutive workouts without legs!", newlyAwarded);
        }

        // UPSIDEDOWN_CHIP - first back workout (must be first time ever)
        if ((sessionHasMovementArea(justSavedSessionLog, "lats") || sessionHasMovementArea(justSavedSessionLog, "back"))
                && isFirstWorkoutWithBack(subId, justSavedSessionLog.getId())) {
            tryAward(user, MedalName.UPSIDEDOWN_CHIP, "First back workout!", newlyAwarded);
        }

        // AINT_NOTHING_BUT_A_PEANUT - first 335
        if (user.getHighest_weight_lifted() >= 335) {
            tryAward(user, MedalName.AINT_NOTHING_BUT_A_PEANUT, "First 335 lb lift!", newlyAwarded);
        }

        // ONE_TIRED_SOB - workout 3+ hours
        if (justSavedSessionLog.getTimeSpentInGym() >= 10800) {
            tryAward(user, MedalName.ONE_TIRED_SOB, "3+ hour workout!", newlyAwarded);
        }

        // NOT_A_CREATURE_WAS_STIRRING - workout before 5 am
        ZonedDateTime zdt = justSavedSessionLog.getDate().atZone(ZoneId.systemDefault());
        if (zdt.toLocalTime().isBefore(LocalTime.of(5, 0))) {
            tryAward(user, MedalName.NOT_A_CREATURE_WAS_STIRRING, "Workout before 5 am!", newlyAwarded);
        }

        // THE_GYM_IS_MY_CHURCH - workout on Sunday
        if (zdt.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            tryAward(user, MedalName.THE_GYM_IS_MY_CHURCH, "Workout on Sunday!", newlyAwarded);
        }

        // WEREWOLF - workout between 9pm and 11:59pm
        int hour = zdt.getHour();
        if (hour >= 21) {
            tryAward(user, MedalName.WEREWOLF, "Late night workout (9pm-12am)!", newlyAwarded);
        }

        // SAME_TIME_TOMORROW - same time 5 days in a row
        if (hasSameTimeStreak(subId, SAME_TIME_DAYS)) {
            tryAward(user, MedalName.SAME_TIME_TOMORROW, "Same time 5 days in a row!", newlyAwarded);
        }

        // PLATES_BEFORE_DATES - workout on Valentine's Day
        if (zdt.getMonthValue() == 2 && zdt.getDayOfMonth() == 14) {
            tryAward(user, MedalName.PLATES_BEFORE_DATES, "Workout on Valentine's Day!", newlyAwarded);
        }

        // SIR_MAX_ALOT - 20+ total sets in workout
        int totalSets = getTotalSets(justSavedSessionLog);
        if (totalSets >= 20) {
            tryAward(user, MedalName.SIR_MAX_ALOT, "20+ sets in one workout!", newlyAwarded);
        }

        // MUSCLE_IS_THE_BEST_GIFT - workout on Christmas
        if (zdt.getMonthValue() == 12 && zdt.getDayOfMonth() == 25) {
            tryAward(user, MedalName.MUSCLE_IS_THE_BEST_GIFT, "Workout on Christmas!", newlyAwarded);
        }

        // IM_DRESSED_AS_AN_OLYMPIAN - workout on Halloween
        if (zdt.getMonthValue() == 10 && zdt.getDayOfMonth() == 31) {
            tryAward(user, MedalName.IM_DRESSED_AS_AN_OLYMPIAN, "Workout on Halloween!", newlyAwarded);
        }

        // SAME_ANIMAL_DIFFERENT_BEAST - first 100 lbs
        if (user.getHighest_weight_lifted() >= 100) {
            tryAward(user, MedalName.SAME_ANIMAL_DIFFERENT_BEAST, "First 100 lb lift!", newlyAwarded);
        }

        // ITS_ABOUT_HOW_YOU_USE_IT - workout max weight doesn't exceed 10
        Double sessionMax = justSavedSessionLog.getSession_highest_lift();
        if (sessionMax != null && sessionMax <= 10 && sessionMax > 0) {
            tryAward(user, MedalName.ITS_ABOUT_HOW_YOU_USE_IT, "Workout with max weight ≤10 lbs!", newlyAwarded);
        }

        // UNICORN - first leg movement (must be first time ever)
        if (sessionHasLegMovement(justSavedSessionLog) && isFirstWorkoutWithArea(subId, justSavedSessionLog.getId(), "quads", "glutes", "hamstrings", "calves")) {
            tryAward(user, MedalName.UNICORN, "Completed a leg movement!", newlyAwarded);
        }

        // GLUTTON_FOR_PUNISHMENT - 2 workouts in the same day
        LocalDate today = justSavedSessionLog.getDate().atZone(ZoneId.systemDefault()).toLocalDate();
        long workoutsToday = sessionLogRepository.findByUser_SubId(subId).stream()
                .filter(s -> s.getDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(today))
                .count();
        if (workoutsToday >= 2) {
            tryAward(user, MedalName.GLUTTON_FOR_PUNISHMENT, "2 workouts in the same day!", newlyAwarded);
        }
        return newlyAwarded;
    }

    /**
     * Checks post-related medals. Call from PostService.createPost.
     */
    @Transactional
    public void checkPostMedals(User user, boolean hasImage) {
        if (user == null || user.getSub_id() == null) return;
        String subId = user.getSub_id();

        // NARCISSUS - 50 posts
        if (postRepository.countByUser_SubId(subId) >= 50) {
            tryAward(user, MedalName.NARCISSUS, "50 posts!");
        }

        // PAPARAZZI_LOVER - 10 posts with pictures
        if (hasImage && postRepository.countByUser_SubIdAndImageLinkNotNull(subId) >= 10) {
            tryAward(user, MedalName.PAPARAZZI_LOVER, "10 posts with pictures!");
        }

        // POEMS_OVER_PRS - 10 posts without pictures
        if (!hasImage && postRepository.countByUser_SubIdAndImageLinkNullOrEmpty(subId) >= 10) {
            tryAward(user, MedalName.POEMS_OVER_PRS, "10 posts without pictures!");
        }
    }

    /**
     * Checks comment medal. Call from PostService.patchPost when comment is added.
     */
    @Transactional
    public void checkCommentMedals(User commenter) {
        if (commenter == null || commenter.getSub_id() == null) return;
        if (commentRepository.countByUser_SubId(commenter.getSub_id()) >= 25) {
            tryAward(commenter, MedalName.HYPE_MAN, "25 comments!");
        }
    }

    /**
     * Checks like medal for post author. Call from PostService.patchPost when like is added.
     */
    @Transactional
    public void checkPostLikeMedals(User postAuthor, int postLikeCount) {
        if (postAuthor == null || postAuthor.getSub_id() == null) return;
        if (postLikeCount >= 50) {
            tryAward(postAuthor, MedalName.INSPIRATION, "50 likes on a post!");
        }
    }

    /**
     * Checks follower medals. Call from FollowService.follow (for the followee).
     */
    @Transactional
    public void checkFollowerMedals(User followee) {
        if (followee == null || followee.getSub_id() == null) return;
        int followers = followee.getNumber_of_followers();
        if (followers >= 300) {
            tryAward(followee, MedalName.THIS_IS_SPARTA, "300 followers!");
        }
        if (followers >= 100) {
            tryAward(followee, MedalName.I_LOVE_BEING_LOVED, "100 followers!");
        }
    }

    /**
     * Checks schedule medal. Call from WorkoutScheduleService create/patch.
     */
    @Transactional
    public void checkScheduleMedals(User user) {
        if (user == null || user.getSub_id() == null) return;
        tryAward(user, MedalName.PLAN_PLAN_PLAN, "Updated your schedule!");
    }

    /**
     * Awards delete medal. Call from SessionLogService when workout is deleted.
     */
    @Transactional
    public void checkDeleteMedal(User user) {
        if (user == null || user.getSub_id() == null) return;
        tryAward(user, MedalName.WE_DONT_TALK_ABOUT_THAT, "Deleted a logged workout!");
    }

    /**
     * Returns all medals for a user, ordered by awardedAt descending.
     */
    @Transactional
    public List<MedalResponse> getMedalsForUser(String subId) {
        if (subId == null || subId.isBlank()) return List.of();
        return userMedalRepository.findByUserSubIdOrderByAwardedAtDesc(subId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns the full catalog of all medals with earned status for the authenticated user.
     * Each item includes medalName, description, earned (true if user has it), and awardedAt (ISO 8601 UTC when earned).
     */
    public List<MedalCatalogItem> getAllMedalsForUser(String subId) {
        if (subId == null || subId.isBlank()) {
            return List.of();
        }
        Map<MedalName, UserMedal> earnedByMedal = userMedalRepository.findByUserSubIdOrderByAwardedAtDesc(subId).stream()
                .collect(Collectors.toMap(UserMedal::getMedalName, m -> m, (a, b) -> a));

        List<MedalCatalogItem> result = new ArrayList<>();
        for (MedalName name : MedalName.values()) {
            UserMedal userMedal = earnedByMedal.get(name);
            boolean earned = userMedal != null;
            String awardedAt = (userMedal != null && userMedal.getAwardedAt() != null)
                    ? userMedal.getAwardedAt().toString()
                    : null;
            result.add(new MedalCatalogItem(name, name.getDescription(), earned, awardedAt));
        }
        return result;
    }

    private MedalResponse toResponse(UserMedal m) {
        String awardedAtStr = m.getAwardedAt() != null
                ? m.getAwardedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        String description = m.getMedalName() != null ? m.getMedalName().getDescription() : null;
        return new MedalResponse(m.getId(), m.getMedalName(), description, awardedAtStr);
    }

    private void tryAward(User user, MedalName medalName, String message) {
        tryAward(user, medalName, message, null);
    }

    private void tryAward(User user, MedalName medalName, String message, List<MedalResponse> newlyAwarded) {
        if (userMedalRepository.existsByUserSubIdAndMedalName(user.getSub_id(), medalName)) return;
        UserMedal medal = new UserMedal();
        medal.setUser(user);
        medal.setMedalName(medalName);
        userMedalRepository.save(medal);
        notificationService.createMedalNotification(user, medal, message);
        if (newlyAwarded != null) {
            newlyAwarded.add(toResponse(medal));
        }
    }

    private boolean sessionHasMovementArea(SessionLog session, String area) {
        List<SessionInstance> instances = session.getSessionInstances();
        if (instances == null || instances.isEmpty()) {
            instances = sessionInstanceRepository.findByWorkoutSessionId(session.getId());
        }
        if (instances == null) return false;
        String areaLower = area.toLowerCase();
        return instances.stream()
                .anyMatch(si -> si.getMovement() != null
                        && si.getMovement().getAreaOfActivation() != null
                        && si.getMovement().getAreaOfActivation().toLowerCase().contains(areaLower));
    }

    private int getTotalSets(SessionLog session) {
        List<SessionInstance> instances = session.getSessionInstances();
        if (instances == null || instances.isEmpty()) {
            instances = sessionInstanceRepository.findByWorkoutSessionId(session.getId());
        }
        if (instances == null) return 0;
        return instances.stream().mapToInt(SessionInstance::getSets).sum();
    }

    private boolean hasConsecutiveWorkoutsWithoutLegs(String subId, int required) {
        List<SessionLog> sessions = sessionLogRepository.findByUser_SubId(subId);
        if (sessions.size() < required) return false;
        Set<LocalDate> dates = sessions.stream()
                .map(s -> s.getDate().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());
        List<LocalDate> sorted = dates.stream().sorted(Comparator.reverseOrder()).toList();
        for (int i = 0; i <= sorted.size() - required; i++) {
            List<LocalDate> window = sorted.subList(i, i + required);
            if (!areConsecutiveDays(window)) continue;
            boolean allWithoutLegs = true;
            for (LocalDate d : window) {
                SessionLog sessionOnDate = sessions.stream()
                        .filter(s -> s.getDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(d))
                        .max(Comparator.comparing(SessionLog::getDate))
                        .orElse(null);
                if (sessionOnDate != null && sessionHasLegMovement(sessionOnDate)) {
                    allWithoutLegs = false;
                    break;
                }
            }
            if (allWithoutLegs) return true;
        }
        return false;
    }

    private boolean sessionHasLegMovement(SessionLog session) {
        return sessionHasMovementArea(session, "quads") || sessionHasMovementArea(session, "glutes")
                || sessionHasMovementArea(session, "hamstrings") || sessionHasMovementArea(session, "calves");
    }

    /**
     * True if this session is the earliest workout (by date) that has any of the given areas.
     * Used for "first X" movement medals.
     */
    private boolean isFirstWorkoutWithArea(String subId, long currentSessionId, String... areas) {
        List<SessionLog> all = sessionLogRepository.findByUser_SubId(subId).stream()
                .sorted(Comparator.comparing(SessionLog::getDate))
                .toList();
        for (SessionLog s : all) {
            boolean hasArea = false;
            for (String area : areas) {
                if (sessionHasMovementArea(s, area)) {
                    hasArea = true;
                    break;
                }
            }
            if (hasArea) {
                return s.getId() == currentSessionId;
            }
        }
        return false;
    }

    private boolean isFirstWorkoutWithBack(String subId, long currentSessionId) {
        return isFirstWorkoutWithArea(subId, currentSessionId, "lats", "back");
    }

    private boolean areConsecutiveDays(List<LocalDate> dates) {
        for (int j = 0; j < dates.size() - 1; j++) {
            if (!dates.get(j).minusDays(1).equals(dates.get(j + 1))) return false;
        }
        return true;
    }

    private boolean hasSameTimeStreak(String subId, int requiredDays) {
        List<SessionLog> sessions = sessionLogRepository.findByUser_SubId(subId);
        if (sessions.size() < requiredDays) return false;
        Map<LocalDate, Set<Integer>> dateToHours = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.mapping(s -> s.getDate().atZone(ZoneId.systemDefault()).getHour(), Collectors.toSet())));
        List<LocalDate> sorted = dateToHours.keySet().stream().sorted(Comparator.reverseOrder()).toList();
        for (int i = 0; i <= sorted.size() - requiredDays; i++) {
            List<LocalDate> window = sorted.subList(i, i + requiredDays);
            if (!areConsecutiveDays(window)) continue;
            Set<Integer> intersection = window.stream()
                    .map(dateToHours::get)
                    .reduce((a, b) -> {
                        Set<Integer> inter = new HashSet<>(a);
                        inter.retainAll(b);
                        return inter;
                    })
                    .orElse(Set.of());
            if (!intersection.isEmpty()) return true;
        }
        return false;
    }
}
