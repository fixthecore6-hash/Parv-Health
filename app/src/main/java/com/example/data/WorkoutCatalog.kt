package com.example.data

enum class ExerciseVisualType {
    PUSHUPS,
    SQUATS,
    LUNGES,
    PLANK,
    GLUTE_BRIDGES,
    WALL_SITS,
    SITUPS,
    JUMPING_JACKS,
    CHEST_PRESS,
    BACK_ROW,
    BAND_PULL,
    PULL_UP,
    SHOULDER_PRESS,
    ARM_CURL,
    CARDIO_RUN
}

data class ExerciseDefinition(
    val id: String,
    val name: String,
    val sets: Int,
    val repsLabel: String,
    val musclesWorked: String,
    val instructions: List<String>,
    val commonMistakes: List<String>,
    val visualType: ExerciseVisualType
)

data class DayWorkoutPlan(
    val dayIndex: Int, // 0 = Mon .. 6 = Sun
    val shortLabel: String,
    val title: String,
    val exercises: List<ExerciseDefinition>
)

object WorkoutCatalog {

    val pushUps = ExerciseDefinition(
        id = "pushups",
        name = "Push-ups",
        sets = 3,
        repsLabel = "8–10 reps",
        musclesWorked = "Chest, shoulders, triceps, and core",
        instructions = listOf(
            "Place your hands slightly wider than shoulder-width on the floor and extend your legs behind you (or rest knees down for an easier option).",
            "Keep your body in a straight line from head to heels by gently tightening your stomach.",
            "Bend your elbows at roughly a 45-degree angle to lower your chest toward the floor.",
            "Press evenly through your palms to return smoothly to the starting position."
        ),
        commonMistakes = listOf(
            "Letting your hips sag toward the floor or lifting them too high.",
            "Flaring your elbows straight out to the sides like a T-shape.",
            "Dropping your chin forward instead of looking slightly ahead."
        ),
        visualType = ExerciseVisualType.PUSHUPS
    )

    val squats = ExerciseDefinition(
        id = "squats",
        name = "Bodyweight Squats",
        sets = 3,
        repsLabel = "12 reps",
        musclesWorked = "Quadriceps, glutes, hamstrings, and core",
        instructions = listOf(
            "Stand with your feet about shoulder-width apart and toes turned slightly outward.",
            "Keep your chest lifted and push your hips back as if sitting into a chair.",
            "Lower until your thighs are roughly parallel to the floor or as deep as feels comfortable.",
            "Drive through your whole foot to stand back up tall."
        ),
        commonMistakes = listOf(
            "Allowing your knees to cave inward toward each other.",
            "Lifting your heels off the floor at the bottom of the squat.",
            "Rounding your lower back instead of keeping your torso upright."
        ),
        visualType = ExerciseVisualType.SQUATS
    )

    val lunges = ExerciseDefinition(
        id = "lunges",
        name = "Forward Lunges",
        sets = 3,
        repsLabel = "10 each leg",
        musclesWorked = "Quadriceps, glutes, hamstrings, and balance stabilizers",
        instructions = listOf(
            "Stand upright with feet hip-width apart and hands resting on your hips or in front of your chest.",
            "Step one foot forward and lower your back knee gently toward the floor until both knees bend near 90 degrees.",
            "Keep your front shin steady with weight centered over the front mid-foot.",
            "Push off your front foot to return to standing, then switch legs."
        ),
        commonMistakes = listOf(
            "Stepping too narrow, which makes balancing harder.",
            "Leaning your torso far forward over your front thigh.",
            "Letting the front knee drift far past your toes while lifting the heel."
        ),
        visualType = ExerciseVisualType.LUNGES
    )

    val plank = ExerciseDefinition(
        id = "plank",
        name = "Forearm Plank",
        sets = 3,
        repsLabel = "25 sec",
        musclesWorked = "Deep core, shoulders, upper back, and glutes",
        instructions = listOf(
            "Rest on your forearms with elbows directly under your shoulders and forearms parallel.",
            "Step both feet back so your weight balances on your toes and forearms.",
            "Brace your stomach and squeeze your glutes to form a straight line from shoulders to ankles.",
            "Breathe steadily through your nose while holding the position for the timer."
        ),
        commonMistakes = listOf(
            "Holding your breath instead of taking slow, steady breaths.",
            "Letting your lower back dip downward as your core tires.",
            "Hunching your shoulders up toward your ears."
        ),
        visualType = ExerciseVisualType.PLANK
    )

    val gluteBridges = ExerciseDefinition(
        id = "glute_bridges",
        name = "Glute Bridges",
        sets = 3,
        repsLabel = "12 reps",
        musclesWorked = "Glutes, hamstrings, and lower back support",
        instructions = listOf(
            "Lie on your back with knees bent, feet flat on the floor hip-width apart, and arms relaxed at your sides.",
            "Press through your heels to lift your hips until your body forms a straight line from shoulders to knees.",
            "Pause for one full second at the top while squeezing your glutes.",
            "Lower your hips slowly with control back to the floor."
        ),
        commonMistakes = listOf(
            "Arching your lower back too high at the top instead of using your glutes.",
            "Placing your feet too close or too far from your hips.",
            "Dropping quickly between reps instead of lowering smoothly."
        ),
        visualType = ExerciseVisualType.GLUTE_BRIDGES
    )

    val wallSits = ExerciseDefinition(
        id = "wall_sits",
        name = "Wall Sits",
        sets = 3,
        repsLabel = "30 sec",
        musclesWorked = "Quadriceps, calves, and lower-body endurance",
        instructions = listOf(
            "Stand with your back flat against a sturdy wall and walk your feet forward about two feet.",
            "Slide your back down the wall until your knees are bent near 90 degrees and ankles sit under your knees.",
            "Keep your shoulders and lower back resting gently against the wall with hands off your thighs.",
            "Hold steadily while breathing evenly until the set finishes."
        ),
        commonMistakes = listOf(
            "Pressing your hands down onto your thighs to reduce leg effort.",
            "Letting your knees push forward past your toes because feet are too close to the wall.",
            "Sliding too low or rising too high instead of holding a steady angle."
        ),
        visualType = ExerciseVisualType.WALL_SITS
    )

    val sitUps = ExerciseDefinition(
        id = "situps",
        name = "Sit-ups",
        sets = 3,
        repsLabel = "10 reps",
        musclesWorked = "Abdominals and hip flexors",
        instructions = listOf(
            "Lie on your back with knees bent, feet flat on the floor, and fingertips resting lightly near your temples or crossed over your chest.",
            "Engage your abdominal muscles to curl your chest up smoothly toward your knees.",
            "Avoid yanking on your neck or swinging your arms for momentum.",
            "Lower your torso back down one vertebra at a time."
        ),
        commonMistakes = listOf(
            "Pulling forward on the back of your head with interlocked fingers.",
            "Using fast momentum to jerk off the floor.",
            "Lifting your feet off the ground as you rise."
        ),
        visualType = ExerciseVisualType.SITUPS
    )

    val jumpingJacks = ExerciseDefinition(
        id = "jumping_jacks",
        name = "Jumping Jacks",
        sets = 3,
        repsLabel = "20 reps",
        musclesWorked = "Full body, calves, shoulders, and cardiovascular endurance",
        instructions = listOf(
            "Start standing tall with feet together and arms resting along your sides.",
            "Jump both feet out wider than hip-width while sweeping both arms smoothly overhead.",
            "Land softly on the balls of your feet with knees slightly bent.",
            "Jump your feet back together as you lower your arms to the starting position."
        ),
        commonMistakes = listOf(
            "Landing flat-footed with stiff, locked knees.",
            "Moving arms halfway instead of completing a full overhead arc.",
            "Rushing faster than you can keep steady posture and breathing."
        ),
        visualType = ExerciseVisualType.JUMPING_JACKS
    )

    val allExercises: List<ExerciseDefinition> = listOf(
        pushUps,
        squats,
        lunges,
        plank,
        gluteBridges,
        wallSits,
        sitUps,
        jumpingJacks
    )

    val weeklySchedule: List<DayWorkoutPlan> = listOf(
        DayWorkoutPlan(
            dayIndex = 0,
            shortLabel = "Mon",
            title = "Upper & Core Starter",
            exercises = listOf(jumpingJacks, pushUps, squats, plank)
        ),
        DayWorkoutPlan(
            dayIndex = 1,
            shortLabel = "Tue",
            title = "Lower Body & Stability",
            exercises = listOf(lunges, gluteBridges, wallSits, sitUps)
        ),
        DayWorkoutPlan(
            dayIndex = 2,
            shortLabel = "Wed",
            title = "Active Flow & Core",
            exercises = listOf(jumpingJacks, plank, gluteBridges)
        ),
        DayWorkoutPlan(
            dayIndex = 3,
            shortLabel = "Thu",
            title = "Full Body Basics",
            exercises = listOf(squats, pushUps, lunges, sitUps)
        ),
        DayWorkoutPlan(
            dayIndex = 4,
            shortLabel = "Fri",
            title = "Strength & Endurance",
            exercises = listOf(jumpingJacks, wallSits, pushUps, plank)
        ),
        DayWorkoutPlan(
            dayIndex = 5,
            shortLabel = "Sat",
            title = "Weekend Mix",
            exercises = listOf(squats, gluteBridges, lunges, jumpingJacks)
        ),
        DayWorkoutPlan(
            dayIndex = 6,
            shortLabel = "Sun",
            title = "Light Mobility & Core",
            exercises = listOf(gluteBridges, plank, sitUps)
        )
    )

    fun planForDay(dayIndex: Int): DayWorkoutPlan {
        return weeklySchedule.getOrElse(dayIndex.coerceIn(0, 6)) { weeklySchedule.first() }
    }
}
