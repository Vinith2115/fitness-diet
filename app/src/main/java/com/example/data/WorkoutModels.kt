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
            name = "Day 1: Core Strength",
            type = "Core",
            description = "Focus on building solid abdominals, hip flexors, and a resilient spine with focused movements.",
            exercises = listOf(
                Exercise(
                    id = "pushups",
                    name = "Pushups",
                    description = "A classic bodyweight exercise that builds chest, shoulder, and core stability.",
                    defaultRepsOrTime = "15 reps",
                    durationSeconds = 40,
                    baseCalories = 15,
                    instructions = listOf(
                        "Start in a plank position with hands slightly wider than shoulders.",
                        "Lower your body until your chest almost touches the floor, keeping your elbows at 45 degrees.",
                        "Keep your torso rigid and core locked; do not let your hips sag.",
                        "Push back up to the starting position with explosive force."
                    ),
                    muscleGroup = "Chest & Core",
                    tips = "Inhale on the way down; exhale as you push up."
                ),
                Exercise(
                    id = "pike_pushups",
                    name = "Pike Pushups",
                    description = "An excellent pushup variation that targets the anterior shoulders and core alignment.",
                    defaultRepsOrTime = "12 reps",
                    durationSeconds = 45,
                    baseCalories = 18,
                    instructions = listOf(
                        "Start in a standard pushup stance, then hike your hips high in the air to form an inverted-V.",
                        "Keep legs and back straight with head aligned between arms.",
                        "Lower the crown of your head slowly toward the floor by bending your elbows.",
                        "Press powerfully back through the shoulders to return to the apex."
                    ),
                    muscleGroup = "Shoulders & Core",
                    tips = "Keep your gaze on your toes to avoid neck strain."
                ),
                Exercise(
                    id = "burpees",
                    name = "Burpees",
                    description = "A high-intensity calorie incinerator that engages all muscle groups.",
                    defaultRepsOrTime = "10 reps",
                    durationSeconds = 35,
                    baseCalories = 25,
                    instructions = listOf(
                        "Stand tall, then drop into a squat and place your hands flat on the floor.",
                        "Jump feet backward into a pushup-ready plank position.",
                        "Jump feet forward back under your hips to return to the deep squat position.",
                        "Explode upward in a high jump, clapping hands behind your head."
                    ),
                    muscleGroup = "Full Body",
                    tips = "Find a steady tempo; do not rush the squat placement."
                ),
                Exercise(
                    id = "plank",
                    name = "Plank Hold",
                    description = "An isometric core exercise that targets deep transverse abdominals.",
                    defaultRepsOrTime = "60 seconds",
                    durationSeconds = 60,
                    baseCalories = 10,
                    instructions = listOf(
                        "Rest forearms on the floor, elbows aligned directly beneath shoulders.",
                        "Extend legs back fully, placing weight on elbows and toes.",
                        "Maintain a strictly flat posture from head to heels.",
                        "Squeeze glutes and abdominals tight while taking deep breaths."
                    ),
                    muscleGroup = "Full Core",
                    tips = "Do not hold your breath. Keep looking down at your hands."
                )
            )
        ),
        WorkoutDay(
            id = "day_2",
            dayNumber = 2,
            name = "Day 2: Cardio Burn",
            type = "Cardio",
            description = "High-octane bodyweight conditioning to elevate heart rate, improve endurance, and torch fat.",
            exercises = listOf(
                Exercise(
                    id = "jumping_jacks",
                    name = "Jumping Jacks",
                    description = "Full body kinetic drills to activate heart rate and mobilize limbs.",
                    defaultRepsOrTime = "40 reps",
                    durationSeconds = 30,
                    baseCalories = 20,
                    instructions = listOf(
                        "Stand with legs aligned together, arms straight down at your sides.",
                        "Jump feet wide to the sides while swinging arms overhead until thumbs kiss.",
                        "Jump back into the start stance, returning arms cleanly to sides."
                    ),
                    muscleGroup = "Cardio",
                    tips = "Land softly on the balls of your feet."
                ),
                Exercise(
                    id = "high_knees",
                    name = "High Knees",
                    description = "A rapid-jogging cardio workout that enhances agility and quadriceps explosive energy.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 30,
                    instructions = listOf(
                        "Jog in place, driving your knees upward as high as possible toward chest level.",
                        "Keep your back upright and avoid leaning backward.",
                        "Pump arms with rhythm to match your leg movement velocity."
                    ),
                    muscleGroup = "Cardio / Legs",
                    tips = "Try to tap your hands with your knees in each stride."
                ),
                Exercise(
                    id = "mountain_climbers",
                    name = "Mountain Climbers",
                    description = "A dynamic upper body hold combined with core and running-leg cadence.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 28,
                    instructions = listOf(
                        "Deconstruct into a high plank position, hands firmly beneath shoulders.",
                        "Drive your right knee up to your chest, keeping hips level and low.",
                        "Switch legs dynamically, extending the right back while running the left forward.",
                        "Keep a swift, synchronized sprint-like motion."
                    ),
                    muscleGroup = "Cardio & Core",
                    tips = "Avoid bouncing your hips in the air."
                ),
                Exercise(
                    id = "sprints",
                    name = "Stationary Sprints",
                    description = "Max-effort explosive speed intervals that elevate metabolic burn.",
                    defaultRepsOrTime = "30 seconds",
                    durationSeconds = 30,
                    baseCalories = 35,
                    instructions = listOf(
                        "Stand slightly leaned forward in a starting blocks position.",
                        "Sprint in place at 100% effort, driving arms and pumping feet as fast as humanly possible.",
                        "Stay on your toes and squeeze your breath."
                    ),
                    muscleGroup = "Cardio / Quads",
                    tips = "Give absolute maximum effort for the entire duration."
                )
            )
        ),
        WorkoutDay(
            id = "day_3",
            dayNumber = 3,
            name = "Day 3: Lower Body Sculpt",
            type = "Lower Body",
            description = "Sculpt and fortify your lower body kinetic chain using highly functional movements.",
            exercises = listOf(
                Exercise(
                    id = "squats",
                    name = "Bodyweight Squats",
                    description = "The foundational lower body lift for reinforcing quads, hamstrings, and gluteal muscles.",
                    defaultRepsOrTime = "20 reps",
                    durationSeconds = 45,
                    baseCalories = 15,
                    instructions = listOf(
                        "Stand with feet shoulder-width apart, toes pointed slightly outward.",
                        "Hinge at the hips and bend knees, acting as if sitting on an imaginary stool.",
                        "Lower until thighs are parallel to the floor, chest high and knees behind toes.",
                        "Drive down through the heels to rise back tall."
                    ),
                    muscleGroup = "Quads & Glutes",
                    tips = "Keep your core engaged to brace your lower back."
                ),
                Exercise(
                    id = "lunges",
                    name = "Forward Lunges",
                    description = "Unilateral strength workout that fixes muscular imbalances and improves leg stability.",
                    defaultRepsOrTime = "16 reps",
                    durationSeconds = 45,
                    baseCalories = 18,
                    instructions = listOf(
                        "Stand with feet parallel, hands on hips or at chest.",
                        "Take a large step forward with your right leg.",
                        "Lower till both knees make 90-degree angles, back knee hovering just off the surface.",
                        "Push back through the front heel to starting stance and switch feet."
                    ),
                    muscleGroup = "Hamstrings & Quads",
                    tips = "Keep your chest tall; do not let your torso tilt forward."
                ),
                Exercise(
                    id = "glute_bridges",
                    name = "Glute Bridges",
                    description = "A powerful posterior-chain glute activator that takes pressure off the lower back.",
                    defaultRepsOrTime = "15 reps",
                    durationSeconds = 40,
                    baseCalories = 12,
                    instructions = listOf(
                        "Lie on your back, knees bent, feet flat on the floor hip-width apart.",
                        "Squeeze glutes and drive heels down to raise hips until torso creates a straight ramp.",
                        "Pause and clamp the glutes for 1-2 seconds at the peak.",
                        "Lower back down slowly."
                    ),
                    muscleGroup = "Glutes & Lower Back",
                    tips = "Avoid over-extending your back; drive from the glutes."
                ),
                Exercise(
                    id = "wall_sit",
                    name = "Wall Sit Hold",
                    description = "An isometric leg burner that tests endurance in the quadriceps under constant load.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 14,
                    instructions = listOf(
                        "Lean your back completely flat against a wall.",
                        "Slide down until thighs are parallel to the floor, simulating sitting on a leg chair.",
                        "Keep ankles directly under your knees.",
                        "Extend arms out or keep hands on the chest, holding tight."
                    ),
                    muscleGroup = "Quadriceps",
                    tips = "Do not rest hands on your knees."
                )
            )
        ),
        WorkoutDay(
            id = "day_4",
            dayNumber = 4,
            name = "Day 4: Full Body Power",
            type = "Full Body",
            description = "A multi-joint metabolic circuit combining strength and dynamic stamina triggers.",
            exercises = listOf(
                Exercise(
                    id = "jumping_jacks_full",
                    name = "Jumping Jacks",
                    description = "Mobilizes upper and lower limbs to prime muscular units for work.",
                    defaultRepsOrTime = "25 reps",
                    durationSeconds = 30,
                    baseCalories = 15,
                    instructions = listOf(
                        "Perform clean, rhythmic jumping jacks with broad range.",
                        "Engage core as feet expand outward."
                    ),
                    muscleGroup = "Cardio",
                    tips = "Control the speed and breathe evenly."
                ),
                Exercise(
                    id = "pushups_full",
                    name = "Classic Pushups",
                    description = "Pumping blood to pectoral, deltoid, and abdominal regions.",
                    defaultRepsOrTime = "12 reps",
                    durationSeconds = 30,
                    baseCalories = 16,
                    instructions = listOf(
                        "Form a rigid body line in high plank.",
                        "Drop deep, then fire up immediately to build shoulder chest force."
                    ),
                    muscleGroup = "Chest & Shoulders",
                    tips = "Ensure your shoulder blades contract on lower and expand on rise."
                ),
                Exercise(
                    id = "walking_lunges",
                    name = "Walking Lunges",
                    description = "A dynamic lunge iteration to trigger kinetic flow.",
                    defaultRepsOrTime = "12 reps",
                    durationSeconds = 45,
                    baseCalories = 20,
                    instructions = listOf(
                        "Lunge forward with your left foot, step forward with right foot, repeating in a structured walking stride."
                    ),
                    muscleGroup = "Lower Body",
                    tips = "Maintain straight posture throughout the step-over."
                ),
                Exercise(
                    id = "plank_full",
                    name = "Plank Finisher",
                    description = "Deep isometric abdominal recruitment to conclude the full-body block.",
                    defaultRepsOrTime = "45 seconds",
                    durationSeconds = 45,
                    baseCalories = 12,
                    instructions = listOf(
                        "Hold high or elbow plank, breathing deeply through muscles while staying perfectly motionless."
                    ),
                    muscleGroup = "Core Integrity",
                    tips = "Clamp glutes and lower abdomen."
                )
            )
        )
    )

    val defaultMeals = listOf(
        // Lose Weight
        PredefinedMeal("Breakfast", "Fresh Avocado Toast with 2 Poached Eggs", 350, 18, "Lose Weight"),
        PredefinedMeal("Lunch", "Grilled Lemon Herb Chicken Bowl with Lettuce & Quinoa", 450, 42, "Lose Weight"),
        PredefinedMeal("Snack", "Greek Yogurt (Non-Fat) with Berries & Honey", 180, 15, "Lose Weight"),
        PredefinedMeal("Dinner", "Baked Ginger Salmon over Steamed Asparagus", 380, 35, "Lose Weight"),

        // Build Muscle
        PredefinedMeal("Breakfast", "Nut Butter & Banana Power Oatmeal with Whey Scoop", 550, 35, "Build Muscle"),
        PredefinedMeal("Lunch", "Double Chicken Fajita Rice Bowl with Black Beans & Cheese", 750, 55, "Build Muscle"),
        PredefinedMeal("Snack", "Mixed Nut Medley (Almonds, Walnuts) & Protein Shake", 420, 30, "Build Muscle"),
        PredefinedMeal("Dinner", "Lean Top Sirloin Steak with Roasted Sweet Potatoes & Broccoli", 680, 50, "Build Muscle"),

        // Stay Fit
        PredefinedMeal("Breakfast", "Sautéed Spinach & Mushroom Omelette (3 Eggs) with Rye", 420, 24, "Stay Fit"),
        PredefinedMeal("Lunch", "Turkey Club Wrap with Whole Wheat Tortilla & Side Salad", 520, 35, "Stay Fit"),
        PredefinedMeal("Snack", "Apple Slices with Almond Butter Bowl", 220, 6, "Stay Fit"),
        PredefinedMeal("Dinner", "Mediterranean Garlic Shrimp Pasta with Feta & Olive Oil", 550, 38, "Stay Fit"),

        // Endurance
        PredefinedMeal("Breakfast", "Honey Sweetened Granola porridge with Mixed Seeds & Banana", 480, 14, "Build Endurance"),
        PredefinedMeal("Lunch", "Teriyaki Tofu Stir-Fry with Brown Rice & Mixed Veggies", 580, 22, "Build Endurance"),
        PredefinedMeal("Snack", "Oat-Honey Energy Bar & Orange", 250, 8, "Build Endurance"),
        PredefinedMeal("Dinner", "Slow-Cooked Pulled Chicken Flatbread with Hummus & Roasted Tomatoes", 600, 45, "Build Endurance")
    )
}
