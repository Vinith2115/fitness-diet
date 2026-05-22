package com.example.data

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val defaultRepsOrTime: String,
    val durationSeconds: Int,
    val baseCalories: Int,
    val instructions: List<String>,
    val muscleGroup: String,
    val tips: String
)

data class WorkoutDay(
    val id: String,
    val dayNumber: Int, // 1, 2, 3, 4
    val name: String,
    val type: String, // "Core", "Cardio", "Lower Body", "Full Body"
    val description: String,
    val exercises: List<Exercise>
)

data class PredefinedMeal(
    val mealType: String, // "Breakfast", "Lunch", "Snack", "Dinner"
    val foodName: String,
    val calories: Int,
    val proteinGrams: Int,
    val goalMatch: String // "Lose Weight", "Build Muscle", "Stay Fit", "Endurance"
)

object StaticWorkoutData {
    val workoutDays = listOf(
        WorkoutDay(
            id = "day_1",
            dayNumber = 1,
            name = "Day 1: Core & Upper Body (Home / Gym)",
            type = "Core",
            description = "Reinforce chest, shoulders, and midsection stability with bodyweight pushups or dumbbells and solid plank holds.",
            exercises = listOf(
                Exercise(
                    id = "pushups",
                    name = "Pushups / DB Bench Press",
                    description = "Build pectoral and core strength. Use dumbbells at the gym or bodyweight at home.",
                    defaultRepsOrTime = "15 reps",
                    durationSeconds = 40,
                    baseCalories = 15,
                    instructions = listOf(
                        "For Home: Set up in a plank position and lower chest to the floor.",
                        "For Gym: Lie on a bench and press dumbbells upward synchronously.",
                        "Maintain a rigid spine throughout the movement."
                    ),
                    muscleGroup = "Chest & Core",
                    tips = "Don't let your hips sag; keep core braced."
                ),
                Exercise(
                    id = "pike_pushups",
                    name = "Pike Pushups / DB Shoulder Press",
                    description = "Focus on shoulder hypertrophy. Perform pike pushups at home or dumbbell overhead press at the gym.",
                    defaultRepsOrTime = "12 reps",
                    durationSeconds = 45,
                    baseCalories = 18,
                    instructions = listOf(
                        "For Home: Walk feet towards hands in a V-shape, lower crown of head.",
                        "For Gym: Sit on a bench and press dumbbells overhead.",
                        "Lock out shoulders at the top position."
                    ),
                    muscleGroup = "Shoulders & Core",
                    tips = "Keep elbows tucked to protect rotator cuffs."
                ),
                Exercise(
                    id = "plank",
                    name = "Plank Hold (Home / Gym)",
                    description = "Isometric core holds to secure the lower back and activate abdominals.",
                    defaultRepsOrTime = "60 seconds",
                    durationSeconds = 60,
                    baseCalories = 10,
                    instructions = listOf(
                        "Rest forearms flat on the floor or mat.",
                        "Align elbows directly under shoulders.",
                        "Squeeze glutes and draw navel in, holding a straight line."
                    ),
                    muscleGroup = "Full Core",
                    tips = "Breathe steadily; don't hold your breath."
                ),
                Exercise(
                    id = "russian_twists",
                    name = "Russian Twists",
                    description = "Targets obliques. Can be performed with a medicine ball/dumbbell at the gym or bodyweight at home.",
                    defaultRepsOrTime = "25 reps",
                    durationSeconds = 40,
                    baseCalories = 12,
                    instructions = listOf(
                        "Sit with knees bent, feet slightly hovered off the floor.",
                        "Lean back at 45 degrees, keeping back straight.",
                        "Twist torso from side to side, touching the floor with hands or weight."
                    ),
                    muscleGroup = "Obliques",
                    tips = "Follow your hands with your gaze to maximize twist."
                )
            )
        ),
        WorkoutDay(
            id = "day_2",
            dayNumber = 2,
            name = "Day 2: Cardio, Jogging & Running (Outdoor / Gym)",
            type = "Cardio",
            description = "Build aerobic stamina and burn calories. Run or jog on a gym treadmill or outdoors in a park.",
            exercises = listOf(
                Exercise(
                    id = "jogging",
                    name = "Jogging (Gym Treadmill / Outdoor)",
                    description = "A moderate-pace jog to improve cardiovascular capacity and warm up joints.",
                    defaultRepsOrTime = "10 minutes",
                    durationSeconds = 600,
                    baseCalories = 80,
                    instructions = listOf(
                        "Maintain a steady, comfortable running pace.",
                        "Land on the midfoot and keep shoulders relaxed.",
                        "Can be done on a gym treadmill or outdoors."
                    ),
                    muscleGroup = "Cardio / Legs",
                    tips = "Keep a conversational pace to build endurance."
                ),
                Exercise(
                    id = "running",
                    name = "Running (Gym Treadmill / Outdoor)",
                    description = "Intense interval running session to burn fat and increase lung capacity.",
                    defaultRepsOrTime = "15 minutes",
                    durationSeconds = 900,
                    baseCalories = 150,
                    instructions = listOf(
                        "Accelerate to a challenging run speed.",
                        "Pump arms in sync with high strides.",
                        "Ideal on a treadmill with incline or an outdoor flat track."
                    ),
                    muscleGroup = "Cardio / Calves",
                    tips = "Keep core engaged and breathe deeply through your nose."
                ),
                Exercise(
                    id = "high_knees",
                    name = "High Knees (Stationary)",
                    description = "Rapid high-knee running in place to spike the heart rate.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 30,
                    instructions = listOf(
                        "Jog in place, driving knees up to hip height.",
                        "Pump arms dynamically.",
                        "Stay light on the balls of your feet."
                    ),
                    muscleGroup = "Cardio / Quads",
                    tips = "Engage your lower abs to lift knees higher."
                ),
                Exercise(
                    id = "jumping_jacks",
                    name = "Jumping Jacks (Home / Gym)",
                    description = "Classic full-body kinetic conditioner.",
                    defaultRepsOrTime = "40 reps",
                    durationSeconds = 30,
                    baseCalories = 20,
                    instructions = listOf(
                        "Start with feet together, arms at sides.",
                        "Jump wide while swinging arms overhead.",
                        "Return to starting stance smoothly."
                    ),
                    muscleGroup = "Cardio",
                    tips = "Land softly to avoid knee impact."
                )
            )
        ),
        WorkoutDay(
            id = "day_3",
            dayNumber = 3,
            name = "Day 3: Lower Body Sculpt (Home / Gym)",
            type = "Lower Body",
            description = "Reinforce the lower chain. Perform bodyweight movements at home or add dumbbells at the gym.",
            exercises = listOf(
                Exercise(
                    id = "squats",
                    name = "DB Goblet Squats / BW Squats",
                    description = "Foundational lower lift. Hold a dumbbell at the chest in the gym, or perform bodyweight squats at home.",
                    defaultRepsOrTime = "20 reps",
                    durationSeconds = 45,
                    baseCalories = 15,
                    instructions = listOf(
                        "Stand with feet shoulder-width apart.",
                        "Hinge at the hips and sit back, keeping chest high.",
                        "Lower until thighs are parallel to the floor, then drive through heels."
                    ),
                    muscleGroup = "Quads & Glutes",
                    tips = "Keep knees tracking in line with toes."
                ),
                Exercise(
                    id = "lunges",
                    name = "Lunges (Dumbbell / Bodyweight)",
                    description = "Unilateral lower body alignment. Hold dumbbells at sides in gym or step bodyweight at home.",
                    defaultRepsOrTime = "16 reps",
                    durationSeconds = 45,
                    baseCalories = 18,
                    instructions = listOf(
                        "Take a large step forward, dropping hips vertically.",
                        "Both knees should bend at 90 degrees.",
                        "Push off front foot to return to standing."
                    ),
                    muscleGroup = "Hamstrings & Quads",
                    tips = "Do not let front knee pass your toes."
                ),
                Exercise(
                    id = "glute_bridges",
                    name = "Glute Bridges (Home / Gym)",
                    description = "Posterior-chain glute activator. Can be loaded with a barbell/dumbbell at the gym.",
                    defaultRepsOrTime = "15 reps",
                    durationSeconds = 40,
                    baseCalories = 12,
                    instructions = listOf(
                        "Lie on back with knees bent and feet flat on the floor.",
                        "Squeeze glutes and raise hips toward the ceiling.",
                        "Hold at the top for 2 seconds before lowering."
                    ),
                    muscleGroup = "Glutes & Hamstrings",
                    tips = "Drive through heels, not your toes."
                ),
                Exercise(
                    id = "calf_raises",
                    name = "Calf Raises (Weighted / BW)",
                    description = "Sculpt and strengthen calf muscles. Use a step platform at the gym or flat ground at home.",
                    defaultRepsOrTime = "30 reps",
                    durationSeconds = 30,
                    baseCalories = 10,
                    instructions = listOf(
                        "Stand straight, feet hip-width apart.",
                        "Rise onto the balls of your feet, squeezing calves at the peak.",
                        "Lower back down slowly with control."
                    ),
                    muscleGroup = "Calves",
                    tips = "Perform slowly to maximize muscle tension."
                )
            )
        ),
        WorkoutDay(
            id = "day_4",
            dayNumber = 4,
            name = "Day 4: Full Body Power & Speed (Home / Gym)",
            type = "Full Body",
            description = "A intense combination of cardiorespiratory speed drills and total body muscle activation.",
            exercises = listOf(
                Exercise(
                    id = "burpees",
                    name = "Burpees / KB Swings",
                    description = "Power movements. Do bodyweight burpees at home or kettlebell swings at the gym.",
                    defaultRepsOrTime = "10 reps",
                    durationSeconds = 35,
                    baseCalories = 25,
                    instructions = listOf(
                        "For Home: Drop to a pushup, jump feet in, and leap up.",
                        "For Gym: Hinge at hips and swing kettlebell to shoulder height.",
                        "Engage core and glutes to drive the power."
                    ),
                    muscleGroup = "Full Body Power",
                    tips = "Maintain standard spinal alignment."
                ),
                Exercise(
                    id = "mountain_climbers",
                    name = "Mountain Climbers",
                    description = "Rapid core stabilizer and cardio builder.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 28,
                    instructions = listOf(
                        "Start in a high plank position.",
                        "Alternate driving knees to chest in a rapid running motion.",
                        "Keep hips low and aligned."
                    ),
                    muscleGroup = "Cardio & Core",
                    tips = "Do not bounce your hips; keep them stable."
                ),
                Exercise(
                    id = "finish_run",
                    name = "Running (Gym Treadmill / Outdoor)",
                    description = "Finisher running session to exhaust energy stores and build endurance.",
                    defaultRepsOrTime = "10 minutes",
                    durationSeconds = 600,
                    baseCalories = 100,
                    instructions = listOf(
                        "Transition into a focused, steady outdoor run or treadmill sprint.",
                        "Focus on regular breathing cycles.",
                        "Push through muscular fatigue safely."
                    ),
                    muscleGroup = "Cardio & Endurance",
                    tips = "Finish strong; cool down with a slow walk."
                ),
                Exercise(
                    id = "plank_finisher",
                    name = "Plank Finisher (Home / Gym)",
                    description = "Final isometric hold to burn out core muscles.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 12,
                    instructions = listOf(
                        "Maintain standard plank position.",
                        "Squeeze all core muscles for the remaining seconds.",
                        "Relax shoulders."
                    ),
                    muscleGroup = "Core Integrity",
                    tips = "Visualize solid posture to keep holding."
                )
            )
        )
    )

    val defaultMeals = listOf(
        // Lose Weight (South Indian, Egg Prioritized)
        PredefinedMeal("Breakfast", "Boiled Egg Podi Idli (2 Idlis) & 2 Boiled Eggs", 320, 18, "Lose Weight"),
        PredefinedMeal("Lunch", "Millet Rice with Rasam, Egg Thokku (2 Eggs) & Cabbage Poriyal", 420, 22, "Lose Weight"),
        PredefinedMeal("Snack", "Boiled Chickpea Sundal & 2 Egg Whites", 160, 14, "Lose Weight"),
        PredefinedMeal("Dinner", "Ragi Dosa (2) with Coconut Chutney & Egg Bhurji (2 Eggs)", 350, 18, "Lose Weight"),

        // Build Muscle (South Indian, Egg Prioritized)
        PredefinedMeal("Breakfast", "Egg Dosa (2 Dosas with 3 Eggs) & Coconut Chutney", 580, 32, "Build Muscle"),
        PredefinedMeal("Lunch", "Thalassery Egg Biryani (with 3 Boiled Eggs) & Onion Raita", 720, 38, "Build Muscle"),
        PredefinedMeal("Snack", "Spiced Egg Bhurji (3 Eggs) with 2 Chapatis", 450, 26, "Build Muscle"),
        PredefinedMeal("Dinner", "Ragi Mudde (Ragi Ball) with Egg Kurma (3 Eggs) & Buttermilk", 620, 30, "Build Muscle"),

        // Stay Fit (South Indian, Egg Prioritized)
        PredefinedMeal("Breakfast", "Vegetable Upma with 2 Boiled Eggs & Sambar", 380, 18, "Stay Fit"),
        PredefinedMeal("Lunch", "Sona Masuri Rice with Rasam, Kovakkai Poriyal & Egg Omelette (2 Eggs)", 490, 20, "Stay Fit"),
        PredefinedMeal("Snack", "Boiled Chickpea Sundal & 2 Boiled Eggs", 240, 16, "Stay Fit"),
        PredefinedMeal("Dinner", "Whole Wheat Chapati (2) with Southern Egg Masala (2 Eggs)", 460, 22, "Stay Fit"),

        // Endurance (South Indian, Egg Prioritized)
        PredefinedMeal("Breakfast", "Kerala Puttu with Kadala Curry & 2 Scrambled Eggs", 480, 22, "Build Endurance"),
        PredefinedMeal("Lunch", "Brown Rice with Drumstick Sambar, Keerai Kootu & Egg Roast (2 Eggs)", 530, 24, "Build Endurance"),
        PredefinedMeal("Snack", "Steamed Nendran Banana & 2 Boiled Eggs", 280, 14, "Build Endurance"),
        PredefinedMeal("Dinner", "Idiyappam (3) with Egg Stew (2 Eggs) & Coconut Milk", 510, 22, "Build Endurance")
    )
}
