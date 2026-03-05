package com.MuscleHead.MuscleHead.Medal;

/**
 * Hardcoded medal types. Add your medal values below.
 */
public enum MedalName {

    // ========== ADD YOUR MEDAL VALUES BELOW ==========
    BAPTISM("Complete your first workout"),
    NO_PAIN_NO_GAIN("Complete 5 workouts"),
    CALL_ME_CLOTH_THE_WAY_I_STAY_IRONED("Complete 10 workouts"),
    LIGHT_WEIGHT_BABY("First 225 lb lift"),
    VETERAN("Log 50 workouts"),
    EVERYONE_WANTS_TO_BE_A_BODY_BUILDER("First 405 lb lift"),
    BUT_NOBODY_WANNA_LIFT_THIS_HEAVY_WEIGHT("First 500 lb lift"),
    MENTZER_FLOW("Log a workout under 30 minutes"),
    FREQUENT_FLIER("Log 25 workouts"),
    YEA_I_WORK_HERE("Log 100 workouts"),
    GYM_RAT("Total time in gym exceeds 24 hours"),
    ZHE_PUMP_IS_ZHE_BEST_FEELING("Complete a chest movement"),
    STICK_FIGURE("5 consecutive workouts without hitting legs"),
    UPSIDEDOWN_CHIP("Complete first back workout"),
    AINT_NOTHING_BUT_A_PEANUT("First 335 lb lift"),
    ONE_TIRED_SOB("Log a workout 3 hours or longer"),
    NOT_A_CREATURE_WAS_STIRRING("Complete a workout before 5 am"),
    THE_GYM_IS_MY_CHURCH("Complete a workout on Sunday"),
    WEREWOLF("Complete a workout between 9pm and 11:59pm"),
    SAME_TIME_TOMORROW("Log a workout at the same time 5 days in a row"),
    PLATES_BEFORE_DATES("Log a workout on Valentine's Day"),
    WE_DONT_TALK_ABOUT_THAT("Delete a logged workout"),
    NARCISSUS("Make 50 posts"),
    PAPARAZZI_LOVER("Make 10 posts with pictures"),
    SIR_MAX_ALOT("Log a workout with 20+ total sets"),
    PLAN_PLAN_PLAN("Update your schedule"),
    MUSCLE_IS_THE_BEST_GIFT("Log a workout on Christmas"),
    IM_DRESSED_AS_AN_OLYMPIAN("Log a workout on Halloween"),
    SAME_ANIMAL_DIFFERENT_BEAST("First 100 lb lift"),
    ITS_ABOUT_HOW_YOU_USE_IT("Log a workout with max weight ≤10 lbs"),
    UNICORN("Complete a leg movement"),
    HYPE_MAN("Make 25 comments"),
    INSPIRATION("Get 50 likes on a post"),
    THIS_IS_SPARTA("Get 300 followers"),
    POEMS_OVER_PRS("Make 10 posts without pictures"),
    I_LOVE_BEING_LOVED("Get 100 followers"),
    GLUTTON_FOR_PUNISHMENT("Log 2 workouts in the same day");

    private final String description;

    MedalName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
