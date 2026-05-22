package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkoutAppUi(viewModel: WorkoutViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation state
    var currentTab by remember { mutableStateOf("Workouts") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (profile?.isOnboarded == true) {
                FitnessBottomNav(activeTab = currentTab, onTabSelect = { currentTab = it })
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                profile == null -> {
                    // Quick loading screen
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ActiveEmerald)
                    }
                }
                profile?.isOnboarded == false -> {
                    OnboardingScreen(viewModel = viewModel)
                }
                else -> {
                    // Main Dashboard content
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                        },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            "Workouts" -> WorkoutsTab(viewModel = viewModel)
                            "Diet" -> DietTab(viewModel = viewModel)
                            "Analytics" -> ProgressTab(viewModel = viewModel)
                            "Settings" -> SettingsTab(viewModel = viewModel)
                        }
                    }
                }
            }

            // Global step-by-step timer guide overlay when active
            if (viewModel.isGuideActive && viewModel.activeExercise != null) {
                ExercisePlayerOverlay(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(viewModel: WorkoutViewModel) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper section: Brand Info & Steps indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = ActiveEmerald,
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0x1510B981), CircleShape)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GET ACTIVE & FUEL SMART",
                style = MaterialTheme.typography.labelMedium,
                color = ActiveEmerald,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Personalize Your Plan",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Custom modern Stepper dots
            Row {
                repeat(totalSteps) { idx ->
                    val isCompleted = step > idx + 1
                    val isActive = step == idx + 1
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isActive) ActiveEmerald else if (isCompleted) CoolTeal else MutedSlate.copy(
                                    alpha = 0.3f
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Central section: Dynamic Form content according to the step
        Box(modifier = Modifier.weight(1f, fill = false)) {
            when (step) {
                1 -> OnboardingStep1(viewModel)
                2 -> OnboardingStep2(viewModel)
                3 -> OnboardingStep3(viewModel)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Lower section: Forward & Back buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("onboarding_back_btn")
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (step < totalSteps) {
                        step++
                    } else {
                        viewModel.completeOnboarding()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RegularThemePrimaryColor(step == totalSteps)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (step > 1) 16.dp else 0.dp)
                    .height(52.dp)
                    .testTag("onboarding_next_btn")
            ) {
                Text(
                    text = if (step == totalSteps) "Let's Begin 🔥" else "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalSlate
                )
            }
        }
    }
}

@Composable
fun RegularThemePrimaryColor(isLast: Boolean): Color {
    return if (isLast) IntensityAmber else ActiveEmerald
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingStep1(viewModel: WorkoutViewModel) {
    Column {
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = viewModel.onboardName,
            onValueChange = { viewModel.onboardName = it },
            label = { Text("What is your name?") },
            placeholder = { Text("Enter your name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_name_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ActiveEmerald,
                focusedLabelColor = ActiveEmerald
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Gender",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val genders = listOf("Female", "Male", "Other")
            genders.forEach { gen ->
                val isSelected = viewModel.onboardGender == gen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ActiveEmerald.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) ActiveEmerald else MutedSlate.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.onboardGender = gen }
                        .testTag("gender_option_$gen"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gen,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ActiveEmerald else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Experience Level",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        listOf(
            "Beginner" to "New to workout formats. (1.0x baseline)",
            "Intermediate" to "Active builder. Consistent routine. (1.25x force)",
            "Advanced" to "Hardened athlete. High stamina holds. (1.5x output)"
        ).forEach { (level, desc) ->
            val isSelected = viewModel.onboardExpLevel == level
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) ActiveEmerald else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.onboardExpLevel = level }
                    .testTag("exp_option_$level"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.onboardExpLevel = level },
                        colors = RadioButtonDefaults.colors(selectedColor = ActiveEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = level, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep2(viewModel: WorkoutViewModel) {
    Column {
        Text(
            text = "Set Fitness Goals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        listOf(
            "Lose Weight" to "Burn calorie stores with structured fat-loss cardio and custom deficits.",
            "Build Muscle" to "Strengthen core fibers and lower quads with muscular hypertrophy.",
            "Build Endurance" to "Stretch lungs and build cardio duration limits.",
            "Stay Fit" to "Maintain visual posture, agility, and simple day-to-day dynamic health."
        ).forEach { (goal, desc) ->
            val isSelected = viewModel.onboardGoal == goal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) CoolTeal else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.onboardGoal = goal }
                    .testTag("goal_option_$goal"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (goal) {
                            "Lose Weight" -> Icons.Default.TrendingDown
                            "Build Muscle" -> Icons.Default.FitnessCenter
                            "Build Endurance" -> Icons.Default.LocalFireDepartment
                            else -> Icons.Default.Favorite
                        },
                        contentDescription = null,
                        tint = if (isSelected) CoolTeal else MutedSlate,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = goal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep3(viewModel: WorkoutViewModel) {
    Column {
        Text(
            text = "Parameters & Target Reminders",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = viewModel.onboardHeight,
                onValueChange = { viewModel.onboardHeight = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboard_height_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ActiveEmerald,
                    focusedLabelColor = ActiveEmerald
                )
            )

            OutlinedTextField(
                value = viewModel.onboardWeight,
                onValueChange = { viewModel.onboardWeight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboard_weight_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ActiveEmerald,
                    focusedLabelColor = ActiveEmerald
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.onboardTargetWeight,
            onValueChange = { viewModel.onboardTargetWeight = it },
            label = { Text("Target Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_target_weight_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ActiveEmerald,
                focusedLabelColor = ActiveEmerald
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preferred Workout Reminder Alarm",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = IntensityAmber
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Daily Notification time", fontWeight = FontWeight.Bold)
            }

            var showTimeDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showTimeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SteelSlate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = viewModel.onboardReminderTime, color = ActiveEmerald, fontWeight = FontWeight.ExtraBold)
            }

            if (showTimeDialog) {
                TimePickerDialog(
                    initialTime = viewModel.onboardReminderTime,
                    onDismiss = { showTimeDialog = false },
                    onConfirm = { timedStr ->
                        viewModel.onboardReminderTime = timedStr
                        showTimeDialog = false
                    }
                )
            }
        }
    }
}

// ==========================================
// WORKOUTS TAB
// ==========================================
@Composable
fun WorkoutsTab(viewModel: WorkoutViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Determine current routine day by modulo logic of date hash
    val currentDayIndex = ((selectedDate.hashCode() % 4 + 4) % 4)
    val workoutDay = StaticWorkoutData.workoutDays[currentDayIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        DateNavigationHeader(viewModel = viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting Header matching Bold Typography style
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        val sdfDay = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
                        var parsedDate: Date? = null
                        try {
                            parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                        } catch (e: Exception) { }
                        val dateString = if (parsedDate != null) sdfDay.format(parsedDate) else "TODAY"
                        
                        Text(
                            text = dateString.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hey, ${profile?.name?.split(" ")?.firstOrNull() ?: "Alex"}.",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 36.sp
                        )
                    }
                    
                    // Initials Avatar Circle Box
                    val initials = if (!profile?.name.isNullOrBlank()) {
                        profile?.name?.trim()?.split("\\s+".toRegex())?.take(2)?.map { it.first().uppercase() }?.joinToString("") ?: "AM"
                    } else {
                        "AM"
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Streak & Reward Tiers card
            item {
                val streak by viewModel.streakDays.collectAsStateWithLifecycle()
                val tier = when {
                    streak <= 0 -> "No Tier"
                    streak in 1..2 -> "Bronze Athlete 🥉"
                    streak in 3..6 -> "Silver Athlete 🥈"
                    streak in 7..14 -> "Gold Athlete 🥇"
                    else -> "Platinum Athlete 🏆"
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DoodleStar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(50.dp),
                            color = NeonYellow.copy(alpha = 0.15f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("🔥", fontSize = 32.sp)
                            Column {
                                Text(
                                    text = "DAILY STREAK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonYellow,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "$streak ${if (streak == 1) "Day" else "Days"}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    text = "Reward Tier: $tier",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextGrayMuted
                                )
                            }
                        }
                    }
                }
            }

            // Active Progress block matching reference HTML layout exactly
            item {
                val completedCount = workoutLogs.size
                val total = workoutDay.exercises.size
                val percentage = if (total > 0) (completedCount.toFloat() / total * 100).toInt() else 0

                val percentageAnimate by animateIntAsState(
                    targetValue = percentage,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "percentage_animation"
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GlassBg,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Background decorative doodle dumbbell
                        DoodleDumbbell(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(64.dp)
                                .offset(x = 10.dp, y = 10.dp),
                            color = NeonCyan.copy(alpha = 0.1f)
                        )
                        // Background decorative blur circle
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 16.dp, y = 16.dp)
                                .size(110.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        )

                        Column {
                            Text(
                                text = "ACTIVE PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$percentageAnimate",
                                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                                    fontSize = 60.sp,
                                    lineHeight = 60.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Linear Progress
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = percentage.toFloat() / 100f)
                                            .background(Color.White)
                                    )
                                }
                                
                                val trendText = when {
                                    percentage == 100 -> "Perfect schedule!"
                                    percentage >= 75 -> "Excellent momentum!"
                                    percentage >= 50 -> "Over halfway done!"
                                    percentage > 0 -> "Making solid progress"
                                    else -> "Start your day active"
                                }
                                Text(
                                    text = trendText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Workout summary card (Day XX Dot Core with Expert Plan)
            item {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GlassBg,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DAY 0${currentDayIndex + 1} ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = workoutDay.type.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            // Expert Plan Badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "EXPERT PLAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = workoutDay.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = workoutDay.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Checklist Header
            item {
                Text(
                    text = "TODAY'S CHECKLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Exercise items list with custom layouts & left-bared details
            items(workoutDay.exercises) { exercise ->
                val isCompleted = workoutLogs.any { it.exerciseName.equals(exercise.name, ignoreCase = true) }

                Card(
                    onClick = { viewModel.startExerciseSession(exercise, workoutDay) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_card_${exercise.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left-side accent indicator bar matching reference HTML style
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji block based on routine target
                            val emoji = when {
                                exercise.name.contains("Push", ignoreCase = true) || exercise.name.contains("Burp", ignoreCase = true) -> "💪"
                                exercise.name.contains("Plank", ignoreCase = true) || exercise.name.contains("Yoga", ignoreCase = true) -> "🧘"
                                exercise.name.contains("Squat", ignoreCase = true) || exercise.name.contains("Lung", ignoreCase = true) -> "🦵"
                                exercise.name.contains("Run", ignoreCase = true) || exercise.name.contains("Cardio", ignoreCase = true) || exercise.name.contains("Sprint", ignoreCase = true) -> "🏃"
                                exercise.name.contains("Meal", ignoreCase = true) || exercise.name.contains("Fuel", ignoreCase = true) -> "🥗"
                                exercise.name.contains("Stretch", ignoreCase = true) -> "🤸"
                                else -> "🏋️"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${exercise.defaultRepsOrTime} • ~${(exercise.baseCalories * (profile?.intensityMultiplier ?: 1f)).toInt()} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Circular completion checkbox toggle
                            IconButton(
                                onClick = {
                                    if (isCompleted) {
                                        val match = workoutLogs.firstOrNull { it.exerciseName.equals(exercise.name, ignoreCase = true) }
                                        if (match != null) viewModel.deleteWorkoutLog(match)
                                    } else {
                                        viewModel.logExerciseCompleted(exercise, workoutDay)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = "Toggle Complete",
                                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// DIET PLANNER TAB
// ==========================================
@Composable
fun DietTab(viewModel: WorkoutViewModel) {
    val dietLogs by viewModel.dietLogs.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showAddMealDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        DateNavigationHeader(viewModel = viewModel)

        // Target progress bar
        val calConsumed = dietLogs.filter { it.isEaten }.sumOf { it.calories }
        val protConsumed = dietLogs.filter { it.isEaten }.sumOf { it.proteinGrams }

        val calTarget = profile?.dailyCalorieTarget ?: 2000
        val protTarget = profile?.dailyProteinTarget ?: 120

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gorgeous circular calorimeter meters and card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GlassBg,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Background decorative apple doodle
                        DoodleApple(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(64.dp)
                                .offset(x = 10.dp, y = 10.dp),
                            color = NeonGreen.copy(alpha = 0.12f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "CALORIMETER COMPASS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "Fuel Tracking",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                NutritionBadge(
                                    label = "Calories Consumed",
                                    current = calConsumed,
                                    target = calTarget,
                                    color = NeonGreen,
                                    unit = "kcal"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                NutritionBadge(
                                    label = "Protein Goal Tracker",
                                    current = protConsumed,
                                    target = protTarget,
                                    color = NeonCyan,
                                    unit = "g"
                                )
                            }

                            // Canvas Circle display representing calories consumption
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .weight(0.8f),
                                contentAlignment = Alignment.Center
                            ) {
                                val ratio = if (calTarget > 0) calConsumed.toFloat() / calTarget else 0f
                                val animRatio by animateFloatAsState(targetValue = ratio.coerceAtMost(1f), label = "cal_progress")

                                val outlineColor = GlassBorder
                                val activeArcColor = NeonGreen

                                Canvas(modifier = Modifier.size(90.dp)) {
                                    drawArc(
                                        color = outlineColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = activeArcColor,
                                        startAngle = -90f,
                                        sweepAngle = animRatio * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(ratio * 100).toInt()}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${calTarget - calConsumed}\nleft",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGrayMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Diet sections
            val meals = listOf("Breakfast", "Lunch", "Snack", "Dinner")
            meals.forEach { type ->
                val logsOfType = dietLogs.filter { it.mealType == type }

                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            IconButton(onClick = { showAddMealDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add custom food",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (logsOfType.isEmpty()) {
                            Text(
                                text = "No meals registered yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        } else {
                            logsOfType.forEach { log ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(IntrinsicSize.Min),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (log.isEaten) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left indicator vertical bar for eaten status
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .fillMaxHeight()
                                                .background(if (log.isEaten) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Checkbox(
                                                    checked = log.isEaten,
                                                    onCheckedChange = { viewModel.toggleMealEaten(log) },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = MaterialTheme.colorScheme.primary,
                                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = log.foodName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${log.calories} kcal",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "${log.proteinGrams}g Protein",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(onClick = { viewModel.deleteMealLog(log) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Meal",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onConfirm = { type, food, kcal, prot ->
                viewModel.addCustomMeal(type, food, kcal, prot)
                showAddMealDialog = false
            }
        )
    }
}

@Composable
fun NutritionBadge(label: String, current: Int, target: Int, color: Color, unit: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${current}/${target} $unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        val ratio = if (target > 0) current.toFloat() / target else 0f
        LinearProgressIndicator(
            progress = { ratio.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// ==========================================
// ANALYTICS / PROGRESS TAB
// ==========================================
@Composable
fun ProgressTab(viewModel: WorkoutViewModel) {
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val allWorkoutLogs by viewModel.allWorkoutLogs.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showWeightDialog by remember { mutableStateOf(false) }
    var cupsDrunk by remember { mutableStateOf(4) } // simple water state tracker

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome banner & quick parameters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PROGRESS MATRIX", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary, 
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Visual Analytics", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { showWeightDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .testTag("log_weight_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale, 
                        contentDescription = "Log Current Weight", 
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Real-time statistical dashboard metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Workouts Ran",
                    value = "${allWorkoutLogs.size} logs",
                    subValue = "All-time history",
                    icon = Icons.Default.FitnessCenter,
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Energy Torch",
                    value = "${allWorkoutLogs.sumOf { it.caloriesBurned }} kcal",
                    subValue = "Calories burned",
                    icon = Icons.Default.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Custom Weight Line Chart Card
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Weight Log Progress Trendline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Real-time records mapped in kilograms",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrayMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (weightLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📊 No Weight Logs Recorded Yet.\nTap the scale icon above to register today's weight!",
                                textAlign = TextAlign.Center,
                                color = TextGrayMuted.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        // Custom Weight Line Chart Drawing
                        WeightVisualLineChart(logs = weightLogs)
                    }
                }
            }
        }

        // Modern Water Glass Tracker widget
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Daily Hydration Target",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            text = "Target: 8 cups (250ml each)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrayMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$cupsDrunk / 8",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cups", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.weight(0.8f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (cupsDrunk > 0) cupsDrunk-- },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove, 
                                contentDescription = "Decrease Water", 
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { cupsDrunk++ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink, 
                                contentDescription = "Add Water Glass", 
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Assigned Plan Constants",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Level Modifier", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = TextGrayMuted
                            )
                            Text(
                                text = profile?.experienceLevel ?: "Beginner", 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Intensity Multiplier", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = TextGrayMuted
                            )
                            Text(
                                text = "${profile?.intensityMultiplier ?: 1.0f}x", 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), 
                                color = NeonCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Target Active Min", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = TextGrayMuted
                            )
                            Text(
                                text = "${profile?.targetWorkoutMinutes ?: 30} mins", 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showWeightDialog) {
        LogWeightDialog(
            onDismiss = { showWeightDialog = false },
            onConfirm = { valw ->
                viewModel.addWeightLog(valw)
                Toast.makeText(context, "Logged weight: $valw kg", Toast.LENGTH_SHORT).show()
                showWeightDialog = false
            }
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subValue: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassBg,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.bodySmall, 
                color = TextGrayMuted,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value, 
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Text(
                text = subValue, 
                style = MaterialTheme.typography.labelSmall, 
                color = TextGrayMuted.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WeightVisualLineChart(logs: List<WeightLog>) {
    var minWeight = logs.minOf { it.weightKg }
    var maxWeight = logs.maxOf { it.weightKg }

    // Pad slightly to avoid snapping to high borders
    if (minWeight == maxWeight) {
        minWeight -= 2f
        maxWeight += 2f
    } else {
        val diff = maxWeight - minWeight
        minWeight -= diff * 0.15f
        maxWeight += diff * 0.15f
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalPoints = logs.size

        val chartPath = Path()
        val fillPath = Path()

        val points = logs.mapIndexed { idx, log ->
            val cx = if (totalPoints > 1) {
                idx.toFloat() / (totalPoints - 1) * width
            } else {
                width / 2f
            }
            val cy = height - ((log.weightKg - minWeight) / (maxWeight - minWeight) * height)
            Offset(cx, cy)
        }

        // Draw horizontal help lines
        val lineCount = 3
        for (i in 0..lineCount) {
            val y = height * i / lineCount
            drawLine(
                color = MutedSlate.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (totalPoints == 1) {
            // Just draw one highlighted dot
            drawCircle(
                color = ActiveEmerald,
                radius = 8.dp.toPx(),
                center = points[0]
            )
            drawCircle(
                color = PureWhite,
                radius = 4.dp.toPx(),
                center = points[0]
            )
        } else {
            // Draw smooth continuous graph line
            chartPath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 1 until totalPoints) {
                val prev = points[i - 1]
                val current = points[i]
                // Cubic interpolation for smoothness
                val controlX1 = prev.x + (current.x - prev.x) / 2f
                val controlY1 = prev.y
                val controlX2 = prev.x + (current.x - prev.x) / 2f
                val controlY2 = current.y

                chartPath.cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw transparency gradient underneath line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(ActiveEmerald.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line stroke
            drawPath(
                path = chartPath,
                color = ActiveEmerald,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw active node circles
            points.forEach { pt ->
                drawCircle(
                    color = ActiveEmerald,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = PureWhite,
                    radius = 2.dp.toPx(),
                    center = pt
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = logs.firstOrNull()?.date ?: "", fontSize = 9.sp, color = MutedSlate)
        Text(text = "Latest: ${logs.lastOrNull()?.weightKg ?: 0f} kg", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ActiveEmerald)
        Text(text = logs.lastOrNull()?.date ?: "", fontSize = 9.sp, color = MutedSlate)
    }
}

// ==========================================
// SETTINGS / ALARMS HUB TAB
// ==========================================
@Composable
fun SettingsTab(viewModel: WorkoutViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher for runtime notification permissions on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permissions approved! Reminders set.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications blocked. Please authorize in system settings.", Toast.LENGTH_LONG).show()
        }
    }

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: "Other") }
    var fitnessGoal by remember(profile) { mutableStateOf(profile?.fitnessGoal ?: "Stay Fit") }
    var heightCmStr by remember(profile) { mutableStateOf(profile?.heightCm?.toString() ?: "") }
    var currentWeightKgStr by remember(profile) { mutableStateOf(profile?.currentWeightKg?.toString() ?: "") }
    var targetWeightKgStr by remember(profile) { mutableStateOf(profile?.targetWeightKg?.toString() ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SETTINGS & PROFILE", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = NeonCyan, 
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Control Center", 
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }
        }

        // Reminders clock schedule configuration card
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Fitness Reminders Scheduler",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Triggers local notifications to keep your consistency alive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrayMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm, 
                                contentDescription = null, 
                                tint = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Wakeup Trigger", 
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                                    color = Color.White
                                )
                                Text(
                                    text = "Current: ${profile?.selectedReminderTime ?: "08:00"}", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = TextGrayMuted
                                )
                            }
                        }

                        var showTimerDlg by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                showTimerDlg = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBgSelected),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit, 
                                contentDescription = "Edit Clock", 
                                tint = Color.White, 
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set Time", 
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (showTimerDlg) {
                            TimePickerDialog(
                                initialTime = profile?.selectedReminderTime ?: "08:00",
                                onDismiss = { showTimerDlg = false },
                                onConfirm = { clockStr ->
                                    viewModel.updateReminderTime(clockStr)
                                    showTimerDlg = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Note: Alarms will trigger twice a day: once at your selected morning time, and again in the evening (exactly 12 hours later).",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // On-the-fly Goal Adjustment card
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Adjust Workout Intensity", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Select different experience levels to dynamically scale workout times and targets.", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = TextGrayMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val levels = listOf("Beginner", "Intermediate", "Advanced")
                    levels.forEach { lv ->
                        val active = profile?.experienceLevel == lv
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (active) GlassBgSelected else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (active) NeonGreen else GlassBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.updateExperienceLevel(lv)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lv, 
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold, 
                                color = Color.White
                            )
                            if (active) {
                                Icon(
                                    imageVector = Icons.Default.Check, 
                                    contentDescription = "Active", 
                                    tint = NeonGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Profile Editor & BMI card
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit User Profile", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", color = TextGrayMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val genders = listOf("Female", "Male", "Other")
                            genders.forEach { gen ->
                                val isSelected = gender == gen
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else GlassBg)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonCyan else GlassBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { gender = gen },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gen,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) NeonCyan else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Fitness Goal",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val goals = listOf("Lose Weight", "Build Muscle", "Build Endurance", "Stay Fit")
                            goals.chunked(2).forEach { rowGoals ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rowGoals.forEach { goal ->
                                        val isSelected = fitnessGoal == goal
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) NeonPink.copy(alpha = 0.15f) else GlassBg)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) NeonPink else GlassBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { fitnessGoal = goal },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = goal,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) NeonPink else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = heightCmStr,
                            onValueChange = { heightCmStr = it },
                            label = { Text("Height (cm)", color = TextGrayMuted, style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = currentWeightKgStr,
                            onValueChange = { currentWeightKgStr = it },
                            label = { Text("Weight (kg)", color = TextGrayMuted, style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetWeightKgStr,
                            onValueChange = { targetWeightKgStr = it },
                            label = { Text("Target (kg)", color = TextGrayMuted, style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val h = heightCmStr.toFloatOrNull() ?: 0f
                    val w = currentWeightKgStr.toFloatOrNull() ?: 0f
                    val bmi = if (h > 0f) w / ((h / 100f) * (h / 100f)) else 0f

                    if (bmi > 0f) {
                        val bmiFormatted = String.format(Locale.US, "%.1f", bmi)
                        val (classification, color, advice) = when {
                            bmi < 18.5 -> Triple(
                                "Underweight", 
                                NeonYellow, 
                                "Focus on lean mass. Prioritize high-protein South Indian meals like Egg Dosa, Scrambled Eggs with Puttu, or Thalassery Egg Biryani. Balance with light jogging."
                            )
                            bmi < 25.0 -> Triple(
                                "Normal Weight", 
                                NeonGreen, 
                                "Great job! Maintain your health by incorporating egg-based South Indian meals, regular running or jogging, and active gym/home workout days."
                            )
                            bmi < 30.0 -> Triple(
                                "Overweight", 
                                NeonPink, 
                                "Focus on metabolic health. Engage in routine outdoor jogging/running. Opt for egg-priority meals like Egg Podimas/Thokku with wheat dosa."
                            )
                            else -> Triple(
                                "Obese", 
                                Color.Red, 
                                "Prioritize steady calorie deficit and cardio. Start with daily jogging/running. Supplement with boiled eggs and fiber-rich South Indian food."
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassBgSelected),
                            border = BorderStroke(1.dp, GlassBorderSelected),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dynamic BMI Indicator",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGrayMuted
                                    )
                                    Text(
                                        text = classification,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                        color = color
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bmiFormatted,
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = advice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGrayMuted,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val parsedH = heightCmStr.toFloatOrNull() ?: profile?.heightCm ?: 170f
                            val parsedW = currentWeightKgStr.toFloatOrNull() ?: profile?.currentWeightKg ?: 70f
                            val parsedT = targetWeightKgStr.toFloatOrNull() ?: profile?.targetWeightKg ?: 68f
                            
                            viewModel.updateProfileData(
                                name = name,
                                gender = gender,
                                heightCm = parsedH,
                                currentWeightKg = parsedW,
                                targetWeightKg = parsedT,
                                fitnessGoal = fitnessGoal
                            )
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Save Profile Details",
                            color = PitchBlack,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CredentialLine(property: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = property, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ==========================================
// EXERCISE SESSON PLAYER OVERLAY
// ==========================================
@Composable
fun ExercisePlayerOverlay(viewModel: WorkoutViewModel) {
    val exercise = viewModel.activeExercise ?: return
    val workoutDay = viewModel.activeWorkoutDay ?: return

    val totalSteps = exercise.instructions.size
    var currentStepIdx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalSlate.copy(alpha = 0.98f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block background clicks */ }
            .testTag("exercise_player_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper: Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "STAGE PLAYING",
                    fontSize = 11.sp,
                    color = CoolTeal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                IconButton(
                    onClick = { viewModel.stopExerciseSession() },
                    modifier = Modifier.background(Color(0x20FFFFFF), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close player", tint = PureWhite)
                }
            }

            // Exercise description panel
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = PureWhite
                )
                Text(
                    text = exercise.muscleGroup.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ActiveEmerald,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Interactive circular timer segment
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val fraction = if (exercise.durationSeconds > 0) {
                    viewModel.timerRemainingSeconds.toFloat() / exercise.durationSeconds
                } else 1f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = MutedSlate.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (viewModel.isTimerRunning) IntensityAmber else ActiveEmerald,
                        startAngle = -90f,
                        sweepAngle = fraction * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (viewModel.timerRemainingSeconds > 0) "${viewModel.timerRemainingSeconds}s" else "DONE!",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = "Duration Timer",
                        fontSize = 12.sp,
                        color = MutedSlate
                    )
                }
            }

            // Step-by-Step Instructions block (Video Guide Simulation)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SteelSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step-by-step Video Guide: ${currentStepIdx + 1}/$totalSteps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoolTeal
                        )
                        Row {
                            IconButton(
                                onClick = { if (currentStepIdx > 0) currentStepIdx-- },
                                enabled = currentStepIdx > 0
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev Instruction", tint = PureWhite)
                            }
                            IconButton(
                                onClick = { if (currentStepIdx < totalSteps - 1) currentStepIdx++ },
                                enabled = currentStepIdx < totalSteps - 1
                            ) {
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Instruction", tint = PureWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (exercise.instructions.isNotEmpty()) exercise.instructions[currentStepIdx] else "Perform dynamic movements correctly.",
                        fontSize = 15.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 Trainer tip: ${exercise.tips}",
                        fontSize = 11.sp,
                        color = MutedSlate
                    )
                }
            }

            // Timer controller row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier
                        .size(64.dp)
                        .background(if (viewModel.isTimerRunning) IntensityAmber else ActiveEmerald, CircleShape)
                ) {
                    Icon(
                        imageVector = if (viewModel.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Playback Control",
                        tint = CharcoalSlate,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Button(
                    onClick = {
                        viewModel.logExerciseCompleted(exercise, workoutDay)
                        viewModel.stopExerciseSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoolTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CharcoalSlate)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Skip & Log Complete", color = CharcoalSlate, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// HELPERS, DIALOGS, COMPONENT UTILS
// ==========================================
@Composable
fun FitnessBottomNav(activeTab: String, onTabSelect: (String) -> Unit) {
    NavigationBar(
        modifier = Modifier.testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple("Workouts", Icons.Default.FitnessCenter, Icons.Outlined.FitnessCenter),
            Triple("Diet", Icons.Default.Restaurant, Icons.Outlined.Restaurant),
            Triple("Analytics", Icons.Default.TrendingUp, Icons.Outlined.TrendingUp),
            Triple("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
        )

        navItems.forEach { (tab, filledIcon, outlinedIcon) ->
            val isActive = activeTab == tab
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = if (isActive) filledIcon else outlinedIcon,
                        contentDescription = tab
                    )
                },
                label = { Text(text = tab, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_$tab")
            )
        }
    }
}

@Composable
fun DateNavigationHeader(viewModel: WorkoutViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { viewModel.changeSelectedDate(-1) },
            modifier = Modifier.testTag("date_prev_btn")
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev day")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DATE SELECTOR",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSlate,
                letterSpacing = 1.sp
            )
            Text(
                text = getDisplayDate(selectedDate),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    // Reset to today
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    viewModel.setSpecificDate(sdf.format(Date()))
                }
            )
        }

        IconButton(
            onClick = { viewModel.changeSelectedDate(1) },
            modifier = Modifier.testTag("date_next_btn")
        ) {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next day")
        }
    }
}

fun getDisplayDate(dateString: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    return try {
        val date = parser.parse(dateString) ?: return dateString
        val todayStr = parser.format(Date())
        if (dateString == todayStr) {
            "Today, " + formatter.format(date)
        } else {
            formatter.format(date)
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun TimePickerDialog(initialTime: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val components = initialTime.split(":")
    var hour by remember { mutableStateOf(if (components.size == 2) components[0].toIntOrNull() ?: 8 else 8) }
    var minute by remember { mutableStateOf(if (components.size == 2) components[1].toIntOrNull() ?: 0 else 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Schedule Notification", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Select Workout Remind Hour & Minute", fontSize = 12.sp, color = MutedSlate)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Clock selection simple columns
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (hour < 23) hour++ else hour = 0 }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
                        }
                        Text(text = String.format("%02d", hour), fontSize = 32.sp, fontWeight = FontWeight.Black, color = ActiveEmerald)
                        IconButton(onClick = { if (hour > 0) hour-- else hour = 23 }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }
                    }

                    Text(text = ":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (minute < 59) minute++ else minute = 0 }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
                        }
                        Text(text = String.format("%02d", minute), fontSize = 32.sp, fontWeight = FontWeight.Black, color = ActiveEmerald)
                        IconButton(onClick = { if (minute > 0) minute-- else minute = 59 }) {
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(String.format("%02d:%02d", hour, minute)) },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveEmerald)
            ) {
                Text("Confirm Time", color = CharcoalSlate)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddMealDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int, Int) -> Unit) {
    var mealType by remember { mutableStateOf("Snack") }
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("150") }
    var protein by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Custom Food Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dropdown mock type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val list = listOf("Breakfast", "Lunch", "Snack", "Dinner")
                    list.forEach { type ->
                        val sel = mealType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) ActiveEmerald else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { mealType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sel) CharcoalSlate else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    placeholder = { Text("e.g. Oatmeal bowl") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (foodName.isNotBlank()) {
                        val cal = calories.toIntOrNull() ?: 150
                        val prot = protein.toIntOrNull() ?: 10
                        onConfirm(mealType, foodName, cal, prot)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveEmerald),
                enabled = foodName.isNotBlank()
            ) {
                Text("Log Food 🍎", color = CharcoalSlate)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogWeightDialog(onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var weightInput by remember { mutableStateOf("70.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Current Weight", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter your weight of today below:", fontSize = 12.sp, color = MutedSlate)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_log_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActiveEmerald,
                        focusedLabelColor = ActiveEmerald
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightInput.toFloatOrNull()
                    if (w != null && w > 0) {
                        onConfirm(w)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActiveEmerald),
                modifier = Modifier.testTag("weight_dialog_confirm_btn")
            ) {
                Text("Log Weight", color = CharcoalSlate)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
