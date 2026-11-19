package com.example.data

enum class MuscleGroup(val label: String) {
    CHEST("Chest"),
    BACK("Back"),
    LEGS("Legs"),
    CORE("Core"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    FULL_BODY("Full body"),
    CARDIO("Cardio")
}

enum class ExerciseLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class ExerciseEquipment(val label: String) {
    NONE("None"),
    DUMBBELLS("Dumbbells"),
    RESISTANCE_BAND("Resistance band"),
    PULL_UP_BAR("Pull-up bar"),
    GYM_MACHINE("Gym machine")
}

data class LibraryExercise(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val level: ExerciseLevel,
    val equipment: ExerciseEquipment,
    val sets: Int,
    val repsLabel: String,
    val instructions: List<String>,
    val commonMistakes: List<String>,
    val visualType: ExerciseVisualType,
    val isCustom: Boolean = false,
    val notes: String = ""
)

object ExerciseLibraryCatalog {

    val builtInExercises: List<LibraryExercise> = listOf(
        // ==================== CHEST (8) ====================
        LibraryExercise(
            id = "lib_incline_pushups",
            name = "Incline Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Place your hands slightly wider than shoulder width on a sturdy bench, counter, or wall.",
                "Step your feet back until your head, torso, and heels form one straight line.",
                "Bend your elbows slowly to bring your chest close to the raised surface.",
                "Press smoothly through both palms until your arms straighten without locking."
            ),
            commonMistakes = listOf(
                "Allowing your lower back to arch toward the floor.",
                "Flaring your elbows straight out to the sides."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_standard_pushups",
            name = "Standard Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8–12 reps",
            instructions = listOf(
                "Start in a high plank with wrists under shoulders and feet together on the floor.",
                "Brace your stomach and glutes to keep your spine neutral.",
                "Lower your chest smoothly toward the floor with elbows angled near 45 degrees.",
                "Push the floor away evenly to return to the top position."
            ),
            commonMistakes = listOf(
                "Dropping your hips before your chest descends.",
                "Doing short half-reps instead of moving through a controlled range.",
                "Craning your neck down toward the mat."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_wide_pushups",
            name = "Wide-Grip Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Set your hands a few inches wider than standard push-up position.",
                "Tighten your midsection and keep your gaze slightly ahead of your fingertips.",
                "Lower your chest steadily between your hands until elbows reach a comfortable bend.",
                "Drive firmly through your palms to return to the start."
            ),
            commonMistakes = listOf(
                "Placing hands so wide that the front of the shoulders feels strained.",
                "Letting the ribcage drop faster than the hips."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_decline_pushups",
            name = "Decline Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8–10 reps",
            instructions = listOf(
                "Rest your toes securely on a low step or bench and place hands on the floor under your shoulders.",
                "Engage your core so your hips stay level rather than sagging.",
                "Lower your upper chest smoothly toward the floor with controlled tempo.",
                "Press back up to full arm extension."
            ),
            commonMistakes = listOf(
                "Using a step that is too high and shifting all strain to the neck.",
                "Bouncing out of the bottom position."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_dumbbell_floor_press",
            name = "Dumbbell Floor Press",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Lie flat on your back with knees bent and hold a dumbbell in each hand.",
                "Tuck your upper arms near a 45-degree angle so elbows gently touch the floor.",
                "Press both weights straight up over your chest until arms are extended.",
                "Lower slowly until the backs of your upper arms pause lightly on the mat."
            ),
            commonMistakes = listOf(
                "Dropping elbows hard against the floor on each rep.",
                "Arching the lower back excessively off the ground."
            ),
            visualType = ExerciseVisualType.CHEST_PRESS
        ),
        LibraryExercise(
            id = "lib_dumbbell_flyes",
            name = "Dumbbell Chest Flyes",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Lie on a bench or mat holding light dumbbells above your chest with palms facing inward.",
                "Keep a gentle bend in your elbows throughout the entire motion.",
                "Open your arms wide in a slow arc until you feel a comfortable stretch across your chest.",
                "Bring the weights back together above your chest in the same smooth arc."
            ),
            commonMistakes = listOf(
                "Straightening the elbows completely and stressing the joints.",
                "Lowering the weights too deep below shoulder level."
            ),
            visualType = ExerciseVisualType.CHEST_PRESS
        ),
        LibraryExercise(
            id = "lib_band_chest_press",
            name = "Band Standing Chest Press",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Loop a resistance band behind your upper back and hold one end in each hand.",
                "Step one foot forward for a balanced stance with knees soft.",
                "Press both hands forward at chest height until arms reach straight out.",
                "Control the band back to chest level without letting shoulders roll forward."
            ),
            commonMistakes = listOf(
                "Shrugging shoulders up toward your ears.",
                "Leaning backward instead of bracing your core."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),
        LibraryExercise(
            id = "lib_machine_chest_press",
            name = "Seated Machine Chest Press",
            muscleGroup = MuscleGroup.CHEST,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Adjust the seat height so the machine handles line up with the middle of your chest.",
                "Keep your shoulder blades resting flat against the backrest and feet flat on the floor.",
                "Press the handles forward smoothly until your arms extend without locking elbows.",
                "Return the handles slowly to the start without letting weight plates slam."
            ),
            commonMistakes = listOf(
                "Setting the seat too low so handles press near your neck.",
                "Lifting your upper back off the pad during heavy presses."
            ),
            visualType = ExerciseVisualType.CHEST_PRESS
        ),

        // ==================== BACK (8) ====================
        LibraryExercise(
            id = "lib_superman_hold",
            name = "Floor Back Extensions",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Lie face down on a mat with arms reaching ahead and legs straight behind you.",
                "Look down at the floor so the back of your neck stays long.",
                "Gently lift your chest and hands a few inches off the mat while squeezing your upper back.",
                "Pause for one breath at the top and lower smoothly to the mat."
            ),
            commonMistakes = listOf(
                "Jerking your head upward and straining your neck.",
                "Lifting too high and pinching your lower spine."
            ),
            visualType = ExerciseVisualType.PLANK
        ),
        LibraryExercise(
            id = "lib_prone_snow_angels",
            name = "Prone Reverse Snow Angels",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Lie face down with your forehead resting lightly on a folded towel and arms overhead.",
                "Raise your arms slightly off the floor with palms facing down.",
                "Sweep both arms in a slow wide arc down toward your hips while squeezing shoulder blades together.",
                "Return your arms overhead under full control."
            ),
            commonMistakes = listOf(
                "Rushing the sweep without engaging the mid-back muscles.",
                "Holding your breath during the movement."
            ),
            visualType = ExerciseVisualType.BACK_ROW
        ),
        LibraryExercise(
            id = "lib_dumbbell_bent_row",
            name = "Bent-Over Dumbbell Row",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Stand with feet hip-width apart holding dumbbells, knees slightly bent.",
                "Hinge forward at the hips with a flat back until your torso angles near 45 degrees.",
                "Pull the dumbbells up toward your waist while driving elbows behind you.",
                "Lower the weights smoothly back down with arms fully extended."
            ),
            commonMistakes = listOf(
                "Rounding the spine while bent forward.",
                "Swinging your torso upward to yank the weights."
            ),
            visualType = ExerciseVisualType.BACK_ROW
        ),
        LibraryExercise(
            id = "lib_single_arm_db_row",
            name = "Single-Arm Dumbbell Row",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10 reps each",
            instructions = listOf(
                "Brace one hand and knee on a sturdy bench while holding a dumbbell in the opposite hand.",
                "Keep your back flat and square to the floor.",
                "Pull the dumbbell toward your hip pocket by driving your elbow upward.",
                "Lower the weight cleanly to a full stretch before starting the next rep."
            ),
            commonMistakes = listOf(
                "Twisting the torso open at the top of the row.",
                "Shrugging the working shoulder toward your ear."
            ),
            visualType = ExerciseVisualType.BACK_ROW
        ),
        LibraryExercise(
            id = "lib_band_seated_row",
            name = "Seated Band Row",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Sit tall on the floor with legs extended and loop a resistance band around both feet.",
                "Hold the band ends with palms facing inward and sit up straight.",
                "Pull both elbows straight back past your ribs while squeezing your shoulder blades.",
                "Extend your arms forward slowly without rounding your shoulders."
            ),
            commonMistakes = listOf(
                "Leaning far backward to pull the band.",
                "Letting the band snap forward quickly."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),
        LibraryExercise(
            id = "lib_dead_hang",
            name = "Bar Dead Hang",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.PULL_UP_BAR,
            sets = 3,
            repsLabel = "20–30 sec",
            instructions = listOf(
                "Grip a pull-up bar with palms facing away slightly wider than shoulder width.",
                "Step gently off any support so your body hangs straight down.",
                "Pull your shoulders slightly away from your ears so the upper back stays lightly engaged.",
                "Breathe calmly and step down smoothly when the hold finishes."
            ),
            commonMistakes = listOf(
                "Holding your breath and tensing your neck.",
                "Dropping off the bar suddenly with stiff knees."
            ),
            visualType = ExerciseVisualType.PULL_UP
        ),
        LibraryExercise(
            id = "lib_standard_pullup",
            name = "Pull-ups",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.PULL_UP_BAR,
            sets = 3,
            repsLabel = "5–8 reps",
            instructions = listOf(
                "Grip the bar overhead with palms facing away just outside shoulder width.",
                "Engage your core and cross or keep legs straight underneath you.",
                "Pull your chest smoothly toward the bar until your chin clears it.",
                "Lower all the way down under control before starting the next rep."
            ),
            commonMistakes = listOf(
                "Kipping or swinging your legs for momentum.",
                "Stopping halfway down instead of extending arms fully."
            ),
            visualType = ExerciseVisualType.PULL_UP
        ),
        LibraryExercise(
            id = "lib_lat_pulldown",
            name = "Lat Pulldown Machine",
            muscleGroup = MuscleGroup.BACK,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Sit at the pulldown station, secure the thigh pad, and grip the wide bar overhead.",
                "Keep your chest lifted tall with a slight natural lean back.",
                "Pull the bar down to upper-chest height by driving your elbows toward the floor.",
                "Control the bar back overhead until your arms stretch comfortably."
            ),
            commonMistakes = listOf(
                "Pulling the bar behind your neck.",
                "Rocking far backward using lower-back momentum."
            ),
            visualType = ExerciseVisualType.PULL_UP
        ),

        // ==================== LEGS (8) ====================
        LibraryExercise(
            id = "lib_bodyweight_squats",
            name = "Bodyweight Squats",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Stand with feet shoulder-width apart and toes turned slightly outward.",
                "Push your hips back and bend your knees as if sitting into a low chair.",
                "Lower until thighs are near parallel while keeping heels pressed flat.",
                "Drive evenly through both feet to stand tall."
            ),
            commonMistakes = listOf(
                "Allowing your knees to collapse inward toward each other.",
                "Lifting your heels off the floor at the bottom."
            ),
            visualType = ExerciseVisualType.SQUATS
        ),
        LibraryExercise(
            id = "lib_forward_lunges",
            name = "Forward Lunges",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps each",
            instructions = listOf(
                "Stand tall with feet hip-width apart and hands resting at your hips.",
                "Step forward smoothly with one foot and lower both knees toward 90-degree bends.",
                "Keep your front knee aligned above your mid-foot and torso upright.",
                "Push cleanly through your front heel to step back together."
            ),
            commonMistakes = listOf(
                "Stepping too narrow so balance feels wobbly.",
                "Letting the front knee cave inward."
            ),
            visualType = ExerciseVisualType.LUNGES
        ),
        LibraryExercise(
            id = "lib_glute_bridges",
            name = "Glute Bridges",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Lie on your back with knees bent, feet hip-width apart, and arms relaxed by your sides.",
                "Press your heels into the floor and squeeze your glutes to raise your hips.",
                "Pause briefly when shoulders, hips, and knees align in a straight slope.",
                "Lower your hips gently to the floor."
            ),
            commonMistakes = listOf(
                "Over-arching the lower back to gain extra height.",
                "Placing feet so far forward that hamstrings cramp."
            ),
            visualType = ExerciseVisualType.GLUTE_BRIDGES
        ),
        LibraryExercise(
            id = "lib_wall_sit",
            name = "Wall Sit",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "30–40 sec",
            instructions = listOf(
                "Stand with your back flat against a solid wall and step feet forward about two feet.",
                "Slide down until knees are bent near 90 degrees and ankles stay below knees.",
                "Rest hands lightly by your sides instead of pushing on your thighs.",
                "Hold the position with calm, steady breaths."
            ),
            commonMistakes = listOf(
                "Pressing palms down on your knees for extra help.",
                "Letting your knees push forward past your toes."
            ),
            visualType = ExerciseVisualType.WALL_SITS
        ),
        LibraryExercise(
            id = "lib_bulgarian_split_squat",
            name = "Rear-Foot Elevated Split Squat",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8–10 reps each",
            instructions = listOf(
                "Stand two steps ahead of a bench and rest the laces of one foot behind you on it.",
                "Keep your chest proud and bend your front knee to lower straight down.",
                "Descend until your front thigh is close to level with the floor.",
                "Press through your front foot to stand upright cleanly."
            ),
            commonMistakes = listOf(
                "Standing too close to the bench so the front knee jams forward.",
                "Twisting your hips sideways instead of facing forward."
            ),
            visualType = ExerciseVisualType.LUNGES
        ),
        LibraryExercise(
            id = "lib_goblet_squat",
            name = "Dumbbell Goblet Squat",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Hold one dumbbell vertically against your upper chest with both palms cupping the top.",
                "Set feet just outside hip width and keep elbows tucked close to your ribs.",
                "Sit down between your knees while keeping your chest lifted.",
                "Drive smoothly through your feet to stand back up."
            ),
            commonMistakes = listOf(
                "Holding the dumbbell away from your chest and pulling forward.",
                "Rounding your lower back at the bottom of the squat."
            ),
            visualType = ExerciseVisualType.SQUATS
        ),
        LibraryExercise(
            id = "lib_band_lateral_walk",
            name = "Mini-Band Lateral Walk",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12 steps each",
            instructions = listOf(
                "Loop a resistance band just above your ankles or knees and stand in a slight quarter-squat.",
                "Keep tension in the band with toes pointing straight ahead.",
                "Take controlled sideways steps without letting your knees cave inward.",
                "Step an equal number of times back in the opposite direction."
            ),
            commonMistakes = listOf(
                "Dragging the trailing foot inward and losing band tension.",
                "Swaying your upper body side to side."
            ),
            visualType = ExerciseVisualType.SQUATS
        ),
        LibraryExercise(
            id = "lib_machine_leg_press",
            name = "Seated Leg Press Machine",
            muscleGroup = MuscleGroup.LEGS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Sit firmly in the leg press seat with lower back flat and feet shoulder-width on the sled.",
                "Release the safety latches and bend your knees smoothly toward your chest.",
                "Stop before your tailbone lifts off the seat pad.",
                "Press evenly through your mid-foot and heels without locking knees hard at the top."
            ),
            commonMistakes = listOf(
                "Locking the knees straight out under heavy load.",
                "Allowing the lower back to curl off the seat."
            ),
            visualType = ExerciseVisualType.WALL_SITS
        ),

        // ==================== CORE (8) ====================
        LibraryExercise(
            id = "lib_forearm_plank",
            name = "Forearm Plank",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "30–40 sec",
            instructions = listOf(
                "Rest on your forearms with elbows directly beneath your shoulders.",
                "Extend legs behind you on your toes and squeeze your glutes and abs.",
                "Keep a straight, steady line from head to heels while breathing smoothly.",
                "End the hold gently before your lower back begins to sag."
            ),
            commonMistakes = listOf(
                "Dropping your hips toward the floor.",
                "Holding your breath or clasping shoulders tight."
            ),
            visualType = ExerciseVisualType.PLANK
        ),
        LibraryExercise(
            id = "lib_dead_bug",
            name = "Dead Bug",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps each",
            instructions = listOf(
                "Lie on your back with arms pointed straight up and knees bent at 90 degrees over hips.",
                "Press your lower back gently into the floor.",
                "Slowly extend your right arm overhead and left leg toward the floor without arching.",
                "Return smoothly to center and repeat on the opposite arm and leg."
            ),
            commonMistakes = listOf(
                "Letting your lower back lift into a gap off the floor.",
                "Moving arms and legs too quickly without bracing."
            ),
            visualType = ExerciseVisualType.GLUTE_BRIDGES
        ),
        LibraryExercise(
            id = "lib_situps",
            name = "Controlled Sit-ups",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Lie on your back with knees bent, feet flat on the floor, and hands crossed lightly over your chest.",
                "Brace your abdominals and curl your chest smoothly upward toward your thighs.",
                "Keep your feet grounded without swinging your arms for speed.",
                "Lower your back smoothly down one section at a time."
            ),
            commonMistakes = listOf(
                "Yanking on the back of your neck with interlocked fingers.",
                "Jerking off the floor using fast momentum."
            ),
            visualType = ExerciseVisualType.SITUPS
        ),
        LibraryExercise(
            id = "lib_bicycle_crunches",
            name = "Bicycle Crunches",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "16 reps total",
            instructions = listOf(
                "Lie on your back with fingertips resting lightly behind your ears and knees lifted.",
                "Rotate your upper torso smoothly so one shoulder turns toward the opposite knee.",
                "Extend the other leg straight forward just above the floor.",
                "Switch sides in a controlled pedaling rhythm."
            ),
            commonMistakes = listOf(
                "Pulling your chin into your chest with your hands.",
                "Rushing side to side without rotating the ribcage."
            ),
            visualType = ExerciseVisualType.SITUPS
        ),
        LibraryExercise(
            id = "lib_side_plank",
            name = "Side Plank",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "25 sec each",
            instructions = listOf(
                "Lie on one side with your bottom forearm planted flat and elbow under your shoulder.",
                "Stack your feet or stagger them for extra stability.",
                "Lift your hips until your body forms a straight diagonal line.",
                "Hold steadily and switch sides."
            ),
            commonMistakes = listOf(
                "Letting your top shoulder roll forward.",
                "Allowing your hips to sink toward the mat."
            ),
            visualType = ExerciseVisualType.PLANK
        ),
        LibraryExercise(
            id = "lib_hanging_knee_raises",
            name = "Hanging Knee Raises",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.PULL_UP_BAR,
            sets = 3,
            repsLabel = "8–12 reps",
            instructions = listOf(
                "Hang from a pull-up bar with hands shoulder-width apart and body still.",
                "Engage your stomach and tuck both knees smoothly upward toward your chest.",
                "Pause for a moment at the top without swinging backward.",
                "Lower your legs slowly until fully extended."
            ),
            commonMistakes = listOf(
                "Swinging forward and backward for momentum.",
                "Dropping legs rapidly between reps."
            ),
            visualType = ExerciseVisualType.PULL_UP
        ),
        LibraryExercise(
            id = "lib_dumbbell_russian_twist",
            name = "Seated Dumbbell Twist",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "16 twists total",
            instructions = listOf(
                "Sit on a mat with knees bent and heels resting lightly on the ground.",
                "Hold one light dumbbell near your chest and lean your torso back slightly with a flat back.",
                "Rotate your shoulders calmly from left to right.",
                "Keep your hips still so the rotation comes from your torso."
            ),
            commonMistakes = listOf(
                "Moving only the arms side to side without turning the torso.",
                "Slouching your lower back."
            ),
            visualType = ExerciseVisualType.SITUPS
        ),
        LibraryExercise(
            id = "lib_band_pallof_press",
            name = "Band Anti-Rotation Press",
            muscleGroup = MuscleGroup.CORE,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "10 reps each",
            instructions = listOf(
                "Anchor a band at chest height to one side and stand perpendicular holding the handle at your sternum.",
                "Step sideways slightly to create light tension with knees soft.",
                "Press your hands straight forward without letting the band pull your torso sideways.",
                "Return hands to your chest and switch sides after the set."
            ),
            commonMistakes = listOf(
                "Allowing your shoulders to twist toward the anchor point.",
                "Standing with locked knees."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),

        // ==================== SHOULDERS (8) ====================
        LibraryExercise(
            id = "lib_wall_slides",
            name = "Wall Shoulder Slides",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Stand with heels, glutes, and upper back touching a smooth wall.",
                "Raise elbows to shoulder height with the backs of your forearms against the wall.",
                "Slide both arms upward overhead while keeping ribs tucked and forearms near the wall.",
                "Glide smoothly back down to shoulder height."
            ),
            commonMistakes = listOf(
                "Arching your lower back away from the wall to reach higher.",
                "Shrugging shoulders up toward your neck."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_pike_pushups",
            name = "Pike Push-ups",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "6–10 reps",
            instructions = listOf(
                "Start in a high plank and walk your feet forward so your hips lift into an inverted V.",
                "Keep weight balanced across your palms with eyes looking back toward your ankles.",
                "Bend your elbows slowly to lower the top of your head toward a spot slightly ahead of your hands.",
                "Press firmly back up to straight arms."
            ),
            commonMistakes = listOf(
                "Flaring elbows wide instead of tracking slightly backward.",
                "Dropping the hips flat mid-rep."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_dumbbell_overhead_press",
            name = "Dumbbell Overhead Press",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Stand tall or sit upright holding dumbbells at shoulder level with palms facing forward.",
                "Brace your stomach to prevent leaning backward.",
                "Press both weights straight up overhead until arms extend comfortably.",
                "Lower slowly back to shoulder height."
            ),
            commonMistakes = listOf(
                "Arching your lower back when pushing overhead.",
                "Clanging the dumbbells together at the top."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_dumbbell_lateral_raise",
            name = "Dumbbell Lateral Raises",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Stand upright holding light dumbbells along your sides with slight elbow bend.",
                "Raise both arms smoothly out to the sides until hands reach shoulder height.",
                "Lead gently with your elbows while keeping shoulders relaxed away from ears.",
                "Lower slowly to your sides without swinging."
            ),
            commonMistakes = listOf(
                "Using heavy weights and swinging your hips to lift them.",
                "Raising arms well above shoulder level."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_dumbbell_front_raise",
            name = "Dumbbell Front Raises",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Hold light dumbbells in front of your thighs with palms facing toward you.",
                "Keep your torso upright and lift one or both arms straight ahead to eye level.",
                "Pause briefly at the top without leaning backward.",
                "Lower with steady control."
            ),
            commonMistakes = listOf(
                "Rocking backward at the waist to gain height.",
                "Dropping the weights quickly down."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_band_face_pulls",
            name = "Band Face Pulls",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Anchor a resistance band at upper-chest height and hold both sides with thumbs pointing back.",
                "Step back gently until the band has light tension.",
                "Pull the band smoothly toward your forehead while separating your hands and lifting elbows wide.",
                "Return arms forward slowly under full control."
            ),
            commonMistakes = listOf(
                "Dropping your elbows below shoulder height.",
                "Leaning backward instead of stabilizing your stance."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),
        LibraryExercise(
            id = "lib_band_overhead_press",
            name = "Band Overhead Shoulder Press",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Stand securely on the center of a long resistance band with feet shoulder-width apart.",
                "Bring the band handles up to shoulder level with forearms upright.",
                "Press both arms straight overhead while bracing your stomach.",
                "Lower back down smoothly to your shoulders."
            ),
            commonMistakes = listOf(
                "Letting your wrists bend backward under tension.",
                "Flaring the ribcage forward at the top."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_machine_shoulder_press",
            name = "Machine Shoulder Press",
            muscleGroup = MuscleGroup.SHOULDERS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Adjust the machine seat so the handles sit right beside your shoulders.",
                "Rest your back against the pad and grip the handles comfortably.",
                "Push the handles upward until your arms extend without snapping elbows.",
                "Lower slowly to shoulder height."
            ),
            commonMistakes = listOf(
                "Sliding forward off the backrest pad.",
                "Lowering the weight stack hard onto the stops."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),

        // ==================== ARMS (8) ====================
        LibraryExercise(
            id = "lib_bench_tricep_dips",
            name = "Chair Tricep Dips",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Sit on the edge of a sturdy chair and place hands right beside your hips.",
                "Slide your hips forward slightly off the seat with knees bent and feet flat.",
                "Bend your elbows straight behind you to lower gently a few inches.",
                "Press through your palms to return upright."
            ),
            commonMistakes = listOf(
                "Shifting hips far away from the chair and stressing your shoulders.",
                "Dropping down too deep."
            ),
            visualType = ExerciseVisualType.ARM_CURL
        ),
        LibraryExercise(
            id = "lib_close_grip_pushups",
            name = "Close-Grip Push-ups",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8–10 reps",
            instructions = listOf(
                "Take a high plank with hands placed directly inside shoulder width.",
                "Keep your elbows tucked near your ribs as you lower your chest.",
                "Pause near the bottom while keeping your spine straight.",
                "Press strongly through the backs of your arms back to the start."
            ),
            commonMistakes = listOf(
                "Letting elbows flare wide out to the sides.",
                "Allowing your hips to sag down."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_dumbbell_bicep_curl",
            name = "Dumbbell Bicep Curls",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Stand upright holding dumbbells at your sides with palms facing forward.",
                "Pin your upper arms softly near your ribs.",
                "Curl the weights smoothly upward toward your shoulders without swinging your torso.",
                "Lower slowly back to straight arms."
            ),
            commonMistakes = listOf(
                "Swinging your elbows forward and rocking your back.",
                "Dropping the weights quickly on the way down."
            ),
            visualType = ExerciseVisualType.ARM_CURL
        ),
        LibraryExercise(
            id = "lib_dumbbell_hammer_curl",
            name = "Dumbbell Hammer Curls",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Hold a dumbbell in each hand by your sides with palms facing inward toward your thighs.",
                "Keep shoulders relaxed and elbows steady at your sides.",
                "Bend at the elbows to curl the weights up toward your chest.",
                "Lower cleanly under control."
            ),
            commonMistakes = listOf(
                "Rotating wrists inward or outward mid-rep.",
                "Leaning backward to lift heavier weight."
            ),
            visualType = ExerciseVisualType.ARM_CURL
        ),
        LibraryExercise(
            id = "lib_db_overhead_tricep",
            name = "Overhead Dumbbell Extension",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10–12 reps",
            instructions = listOf(
                "Sit or stand tall holding one dumbbell overhead with both hands cupping the top plate.",
                "Keep upper arms pointed up close to your ears.",
                "Bend only at the elbows to lower the weight smoothly behind your head.",
                "Extend arms upward to squeeze the triceps."
            ),
            commonMistakes = listOf(
                "Flaring your elbows wide apart.",
                "Arching your lower back."
            ),
            visualType = ExerciseVisualType.ARM_CURL
        ),
        LibraryExercise(
            id = "lib_band_bicep_curl",
            name = "Band Bicep Curls",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12–15 reps",
            instructions = listOf(
                "Stand firmly on the middle of a resistance band with feet hip-width apart.",
                "Hold the handles with palms facing forward and elbows tucked gently at your sides.",
                "Curl both hands smoothly up toward your shoulders.",
                "Lower slowly so the band does not snap back."
            ),
            commonMistakes = listOf(
                "Pulling elbows back behind your torso.",
                "Bending your wrists downward."
            ),
            visualType = ExerciseVisualType.ARM_CURL
        ),
        LibraryExercise(
            id = "lib_chin_ups",
            name = "Underhand Chin-ups",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.PULL_UP_BAR,
            sets = 3,
            repsLabel = "5–8 reps",
            instructions = listOf(
                "Grip a pull-up bar shoulder-width apart with palms facing toward you.",
                "Start from a controlled hang with chest lifted slightly.",
                "Pull yourself upward until your chin clears the bar.",
                "Lower smoothly back to full arm extension."
            ),
            commonMistakes = listOf(
                "Half-lowering instead of straightening arms completely.",
                "Kicking your knees upward for speed."
            ),
            visualType = ExerciseVisualType.PULL_UP
        ),
        LibraryExercise(
            id = "lib_cable_tricep_pushdown",
            name = "Cable Rope Pushdown",
            muscleGroup = MuscleGroup.ARMS,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Attach a rope handle to a high cable pulley and grip both ends with elbows tucked at your sides.",
                "Stand tall with a slight forward hinge at the hips.",
                "Press the rope handles straight down until your arms extend and wrists stay neutral.",
                "Control the rope back up to chest level."
            ),
            commonMistakes = listOf(
                "Letting elbows drift forward away from your ribs.",
                "Leaning your whole body weight onto the cable."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),

        // ==================== FULL BODY (8) ====================
        LibraryExercise(
            id = "lib_inchworms",
            name = "Inchworm Walkouts",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8 reps",
            instructions = listOf(
                "Stand tall, hinge at the hips with a gentle knee bend, and place your palms on the mat.",
                "Walk your hands steadily forward until you reach a strong high plank.",
                "Pause for one breath with core braced.",
                "Walk your hands smoothly back toward your feet and stand up."
            ),
            commonMistakes = listOf(
                "Letting your hips drop below plank alignment.",
                "Rushing through the walkout without bracing."
            ),
            visualType = ExerciseVisualType.PUSHUPS
        ),
        LibraryExercise(
            id = "lib_stepback_burpees",
            name = "Step-Back Burpees",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "8–10 reps",
            instructions = listOf(
                "Squat down and place both hands firmly on the floor in front of your feet.",
                "Step one foot back at a time into a steady plank position.",
                "Step each foot forward back under your hips.",
                "Stand tall and reach your arms overhead."
            ),
            commonMistakes = listOf(
                "Letting your lower back collapse in the plank.",
                "Holding your breath through the transition."
            ),
            visualType = ExerciseVisualType.JUMPING_JACKS
        ),
        LibraryExercise(
            id = "lib_full_burpees",
            name = "Athletic Burpees",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Drop smoothly into a squat and plant your palms shoulder-width apart.",
                "Hop both feet back into a tight plank with core braced.",
                "Hop your feet forward outside your hands with heels flat.",
                "Explode upright into a light jump and land softly."
            ),
            commonMistakes = listOf(
                "Landing on stiff straight knees after the jump.",
                "Dropping hips lazily in the plank."
            ),
            visualType = ExerciseVisualType.JUMPING_JACKS
        ),
        LibraryExercise(
            id = "lib_bear_crawl",
            name = "Bear Crawl",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "20 steps total",
            instructions = listOf(
                "Start on hands and toes with knees hovering two inches off the floor and back flat.",
                "Step your right hand and left foot forward smoothly together.",
                "Follow with your left hand and right foot while keeping hips steady.",
                "Reverse backward if space is tight."
            ),
            commonMistakes = listOf(
                "Lifting your hips high like a tent.",
                "Swaying side to side on every step."
            ),
            visualType = ExerciseVisualType.PLANK
        ),
        LibraryExercise(
            id = "lib_dumbbell_thrusters",
            name = "Dumbbell Squat to Press",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Hold dumbbells cleanly at shoulder height with feet shoulder-width apart.",
                "Lower smoothly into a controlled squat while keeping your torso upright.",
                "Stand up smoothly and use that leg drive to press the dumbbells overhead.",
                "Lower the weights back to your shoulders before the next squat."
            ),
            commonMistakes = listOf(
                "Pressing before your legs finish standing.",
                "Dropping your chest forward during the squat."
            ),
            visualType = ExerciseVisualType.SHOULDER_PRESS
        ),
        LibraryExercise(
            id = "lib_dumbbell_rdl_row",
            name = "Dumbbell Hinge to Row",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.DUMBBELLS,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Hold dumbbells in front of your thighs and hinge backward at the hips with a flat spine.",
                "Pause near mid-shin height and perform one clean dumbbell row.",
                "Extend your arms back down while holding the flat-back hinge.",
                "Drive your hips forward to stand upright."
            ),
            commonMistakes = listOf(
                "Rounding your lower back while rowing.",
                "Bending your knees too far into a squat."
            ),
            visualType = ExerciseVisualType.BACK_ROW
        ),
        LibraryExercise(
            id = "lib_band_squat_row",
            name = "Band Squat to Row",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.RESISTANCE_BAND,
            sets = 3,
            repsLabel = "12 reps",
            instructions = listOf(
                "Anchor a band at waist height and step back holding both handles with arms extended.",
                "Sit smoothly down into a squat while keeping tension on the band.",
                "As you stand tall, pull both elbows straight back beside your ribs.",
                "Extend arms forward as you reset for the next rep."
            ),
            commonMistakes = listOf(
                "Letting shoulders hunch forward at the start.",
                "Falling backward when sitting into the squat."
            ),
            visualType = ExerciseVisualType.BAND_PULL
        ),
        LibraryExercise(
            id = "lib_rowing_machine",
            name = "Rowing Ergometer",
            muscleGroup = MuscleGroup.FULL_BODY,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "90 sec",
            instructions = listOf(
                "Strap your feet comfortably and sit upright grabbing the handle with arms straight.",
                "Push powerfully back with your legs first while keeping your torso upright.",
                "Lean back slightly and pull the handle smoothly to your lower ribs.",
                "Extend arms forward first, then bend knees to glide back to the catch."
            ),
            commonMistakes = listOf(
                "Bending your arms early before your legs finish pushing.",
                "Rounding your back at the front of the stroke."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        ),

        // ==================== CARDIO (8) ====================
        LibraryExercise(
            id = "lib_jumping_jacks",
            name = "Jumping Jacks",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "25 reps",
            instructions = listOf(
                "Stand tall with feet together and hands resting by your sides.",
                "Hop feet out wider than hips while sweeping both arms smoothly overhead.",
                "Land lightly on the balls of your feet with knees slightly soft.",
                "Hop feet back together as your arms lower cleanly."
            ),
            commonMistakes = listOf(
                "Landing heavily on flat feet with locked knees.",
                "Cutting arm movement short below shoulder height."
            ),
            visualType = ExerciseVisualType.JUMPING_JACKS
        ),
        LibraryExercise(
            id = "lib_high_knees",
            name = "High Knees March or Run",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "30 sec",
            instructions = listOf(
                "Stand upright with core braced and elbows bent near 90 degrees.",
                "Drive one knee up toward waist height while pumping the opposite arm.",
                "Switch legs smoothly at a brisk march or light jog pace.",
                "Land quietly on your forefeet and breathe steadily."
            ),
            commonMistakes = listOf(
                "Leaning backward to lift your knees higher.",
                "Slapping your feet loud onto the floor."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        ),
        LibraryExercise(
            id = "lib_mountain_climbers",
            name = "Mountain Climbers",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "30 sec",
            instructions = listOf(
                "Start in a strong high plank with hands stacked right under your shoulders.",
                "Drive your right knee smoothly toward your chest without bouncing your hips.",
                "Switch legs quickly in a controlled running motion on the spot.",
                "Keep shoulders level over your wrists throughout."
            ),
            commonMistakes = listOf(
                "Bouncing your hips high into the air.",
                "Drifting shoulders far behind your hands."
            ),
            visualType = ExerciseVisualType.PLANK
        ),
        LibraryExercise(
            id = "lib_butt_kicks",
            name = "Heel-to-Glute Jog",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "30 sec",
            instructions = listOf(
                "Stand tall with knees relaxed and arms bent gently at your sides.",
                "Jog lightly in place while snapping alternating heels up toward your glutes.",
                "Keep thighs vertical rather than swinging knees far forward.",
                "Maintain an upright posture and steady breathing."
            ),
            commonMistakes = listOf(
                "Hunching your shoulders forward.",
                "Landing hard on your heels."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        ),
        LibraryExercise(
            id = "lib_skater_hops",
            name = "Lateral Skater Steps",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.INTERMEDIATE,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "20 reps total",
            instructions = listOf(
                "Start balanced slightly on one leg with knees softly bent.",
                "Step or bound sideways onto the opposite foot while sweeping your trailing foot behind.",
                "Stick the landing quietly for a split second with chest up.",
                "Push smoothly back to the starting side."
            ),
            commonMistakes = listOf(
                "Letting your landing knee cave inward.",
                "Rushing sideways without stabilizing balance."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        ),
        LibraryExercise(
            id = "lib_jump_squats",
            name = "Soft Jump Squats",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.ADVANCED,
            equipment = ExerciseEquipment.NONE,
            sets = 3,
            repsLabel = "10 reps",
            instructions = listOf(
                "Lower smoothly into a comfortable half-squat with chest lifted.",
                "Push cleanly through both feet to rise into a small vertical hop.",
                "Land softly toe-to-heel and immediately absorb the landing into bent knees.",
                "Pause briefly before initiating the next rep."
            ),
            commonMistakes = listOf(
                "Landing stiff-legged with loud impact.",
                "Collapsing knees inward on takeoff or landing."
            ),
            visualType = ExerciseVisualType.SQUATS
        ),
        LibraryExercise(
            id = "lib_stationary_bike",
            name = "Stationary Bike Intervals",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 3,
            repsLabel = "2 min",
            instructions = listOf(
                "Adjust seat height so your knee stays slightly bent at the bottom of each pedal stroke.",
                "Set moderate resistance and pedal steadily for a brief warmup minute.",
                "Increase cadence smoothly for a brisk interval while keeping shoulders relaxed.",
                "Spin easily between rounds to let breathing settle."
            ),
            commonMistakes = listOf(
                "Setting the seat too low so knees feel cramped.",
                "Slouching hard over the handlebars."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        ),
        LibraryExercise(
            id = "lib_incline_treadmill",
            name = "Brisk Treadmill Walk",
            muscleGroup = MuscleGroup.CARDIO,
            level = ExerciseLevel.BEGINNER,
            equipment = ExerciseEquipment.GYM_MACHINE,
            sets = 2,
            repsLabel = "5 min",
            instructions = listOf(
                "Step onto the treadmill belt, set a light incline, and start at a comfortable walking speed.",
                "Stand tall without hanging your body weight onto the front handrails.",
                "Swing your arms naturally at your sides and take even strides.",
                "Lower speed slowly for the last minute before stepping off."
            ),
            commonMistakes = listOf(
                "Clinging tightly to the console rails while walking.",
                "Over-striding far ahead of your center of mass."
            ),
            visualType = ExerciseVisualType.CARDIO_RUN
        )
    )

    val defaultWeeklyScheduleMap: Map<Int, List<String>> = mapOf(
        0 to listOf("lib_jumping_jacks", "lib_standard_pushups", "lib_bodyweight_squats", "lib_forearm_plank"),
        1 to listOf("lib_forward_lunges", "lib_glute_bridges", "lib_wall_sit", "lib_situps"),
        2 to listOf("lib_high_knees", "lib_forearm_plank", "lib_superman_hold", "lib_inchworms"),
        3 to listOf("lib_bodyweight_squats", "lib_incline_pushups", "lib_forward_lunges", "lib_dead_bug"),
        4 to listOf("lib_jumping_jacks", "lib_wall_sit", "lib_standard_pushups", "lib_side_plank"),
        5 to listOf("lib_skater_hops", "lib_glute_bridges", "lib_stepback_burpees", "lib_bicycle_crunches"),
        6 to listOf("lib_wall_slides", "lib_glute_bridges", "lib_dead_bug", "lib_high_knees")
    )

    fun findById(id: String): LibraryExercise? = builtInExercises.firstOrNull { it.id == id }
}
