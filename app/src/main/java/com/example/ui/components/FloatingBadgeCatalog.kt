package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Bathtub
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BreakfastDining
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material.icons.rounded.EmojiNature
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Looks
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.RamenDining
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class BadgePopEffect {
    NONE,
    ZZZ_POP,
    ZZZ_BIG,
    SPARKLE_POP,
    BOLT_POP,
    HEART_POP,
    STEAM_POP,
    MOON_POP,
    STAR_POP,
    FLAME_POP,
    DROP_POP,
    CHECK_POP,
    NOTE_POP
}

data class FloatingBadgeCategory(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class FloatingBadgeIconSpec(
    val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameEn: String,
    val drawableRes: Int? = null,
    val gridTileUri: String? = null,
    val vector: ImageVector? = null,
    val bgTop: Color = Color(0xFFEAFBF4),
    val bgBottom: Color = Color(0xFFC8F1E1),
    val iconTint: Color = Color(0xFF0F766E),
    val accentTint: Color = Color(0xFF18C28B),
    val popEffect: BadgePopEffect = BadgePopEffect.NONE
)

object FloatingBadgeCatalog {

    val categories: List<FloatingBadgeCategory> = listOf(
        FloatingBadgeCategory("all", "الكل", "All"),
        FloatingBadgeCategory("bald_expressions", "تعبيرات أصلع 3D", "Bald Expressions"),
        FloatingBadgeCategory("pixar_chars", "شخصيات 3D", "3D Guys"),
        FloatingBadgeCategory("gym", "الجيم والتمارين", "Gym"),
        FloatingBadgeCategory("character", "الشخصية الأصلية", "Original"),
        FloatingBadgeCategory("sleep", "النوم والراحة", "Sleep"),
        FloatingBadgeCategory("active", "النشاط", "Active"),
        FloatingBadgeCategory("walk", "المشي والحركة", "Movement"),
        FloatingBadgeCategory("food", "الطعام", "Food"),
        FloatingBadgeCategory("coffee", "القهوة والمشروبات", "Drinks"),
        FloatingBadgeCategory("relax", "الاسترخاء", "Relax"),
        FloatingBadgeCategory("work", "العمل والدراسة", "Work"),
        FloatingBadgeCategory("mood", "المزاج والرموز", "Moods")
    )

    // Cohesive soft pastel palettes matching the app's original mint-cream aesthetic
    private val mintTop = Color(0xFFEDFBF5); private val mintBot = Color(0xFFC2EFE0)
    private val skyTop = Color(0xFFEBF6FF); private val skyBot = Color(0xFFC2E2FF)
    private val creamTop = Color(0xFFFFF8EA); private val creamBot = Color(0xFFFBE2B8)
    private val peachTop = Color(0xFFFFF1EB); private val peachBot = Color(0xFFFFD4C2)
    private val lavTop = Color(0xFFF3EEFF); private val lavBot = Color(0xFFDCCBFF)
    private val roseTop = Color(0xFFFFEFF5); private val roseBot = Color(0xFFFFCEE0)
    private val sageTop = Color(0xFFEEF7F0); private val sageBot = Color(0xFFC8E6CF)
    private val sandTop = Color(0xFFFAF3E8); private val sandBot = Color(0xFFEAD4B5)
    private val iceTop = Color(0xFFEEF9FC); private val iceBot = Color(0xFFC5EAF5)

    val icons: List<FloatingBadgeIconSpec> = listOf(
        // ==================== 0. BALD MALE PIXAR 3D EXPRESSIONS (9) ====================
        FloatingBadgeIconSpec(
            id = "bald_expr_laugh", categoryId = "bald_expressions",
            nameAr = "أصلع • ضحكة كبيرة", nameEn = "Bald • Big Laugh",
            gridTileUri = "res://img_bald_male_expressions#0",
            accentTint = Color(0xFFEC4899), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_determined", categoryId = "bald_expressions",
            nameAr = "أصلع • تركيز وحماس", nameEn = "Bald • Determined",
            gridTileUri = "res://img_bald_male_expressions#1",
            accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_surprised", categoryId = "bald_expressions",
            nameAr = "أصلع • مندهش ومصدوم", nameEn = "Bald • Surprised",
            gridTileUri = "res://img_bald_male_expressions#2",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_wink", categoryId = "bald_expressions",
            nameAr = "أصلع • غمزة واثقة", nameEn = "Bald • Playful Wink",
            gridTileUri = "res://img_bald_male_expressions#3",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_yawn", categoryId = "bald_expressions",
            nameAr = "أصلع • نعسان يتثاءب", nameEn = "Bald • Sleepy Yawn",
            gridTileUri = "res://img_bald_male_expressions#4",
            accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_grumpy", categoryId = "bald_expressions",
            nameAr = "أصلع • غاضب ومكشّر", nameEn = "Bald • Grumpy Frown",
            gridTileUri = "res://img_bald_male_expressions#5",
            accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_shy", categoryId = "bald_expressions",
            nameAr = "أصلع • خجول ولطيف", nameEn = "Bald • Shy Blush",
            gridTileUri = "res://img_bald_male_expressions#6",
            accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_cool", categoryId = "bald_expressions",
            nameAr = "أصلع • كول بنظارة", nameEn = "Bald • Cool Shades",
            gridTileUri = "res://img_bald_male_expressions#7",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "bald_expr_zen", categoryId = "bald_expressions",
            nameAr = "أصلع • هدوء واسترخاء", nameEn = "Bald • Calm Zen",
            gridTileUri = "res://img_bald_male_expressions#8",
            accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.NONE
        ),

        // ==================== 0B. BALD MALE GYM & CARDIO 3D MINI ICONS (9) ====================
        FloatingBadgeIconSpec(
            id = "bald_gym_bench", categoryId = "gym",
            nameAr = "أصلع • بنش برس", nameEn = "Bald • Bench Press",
            gridTileUri = "res://img_bald_male_workouts_1#0",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_squat", categoryId = "gym",
            nameAr = "أصلع • سكوات رجل", nameEn = "Bald • Barbell Squat",
            gridTileUri = "res://img_bald_male_workouts_1#1",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_deadlift", categoryId = "gym",
            nameAr = "أصلع • ديدليفت", nameEn = "Bald • Deadlift",
            gridTileUri = "res://img_bald_male_workouts_1#2",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_pullups", categoryId = "gym",
            nameAr = "أصلع • عقلة ظهر", nameEn = "Bald • Pull-Ups",
            gridTileUri = "res://img_bald_male_workouts_1#3",
            accentTint = Color(0xFF2563EB), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_curls", categoryId = "gym",
            nameAr = "أصلع • دمبل ذراع", nameEn = "Bald • Bicep Curls",
            gridTileUri = "res://img_bald_male_workouts_1#4",
            accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_treadmill", categoryId = "gym",
            nameAr = "أصلع • جري مشاية", nameEn = "Bald • Treadmill Run",
            gridTileUri = "res://img_bald_male_workouts_2#0",
            accentTint = Color(0xFF06B6D4), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_boxing", categoryId = "gym",
            nameAr = "أصلع • ملاكمة", nameEn = "Bald • Boxing Bag",
            gridTileUri = "res://img_bald_male_workouts_2#2",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_ropes", categoryId = "gym",
            nameAr = "أصلع • حبال القوة", nameEn = "Bald • Battle Ropes",
            gridTileUri = "res://img_bald_male_workouts_2#4",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "bald_gym_protein", categoryId = "gym",
            nameAr = "أصلع • بروتين شيك", nameEn = "Bald • Protein Shake",
            gridTileUri = "res://img_bald_male_workouts_2#7",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),

        // ==================== 1. CHARACTER & STATES (12) ====================
        FloatingBadgeIconSpec(
            id = "sleeping", categoryId = "character",
            nameAr = "نائم (الأصلي)", nameEn = "Sleeping (Original)",
            drawableRes = R.drawable.img_sleepy_badge_1795028747325,
            popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "char_sleeping_zzz", categoryId = "character",
            nameAr = "نائم + Zzz بارزة", nameEn = "Sleeping + Pop Zzz",
            drawableRes = R.drawable.img_sleepy_badge_1795028747325,
            accentTint = Color(0xFF3B82F6),
            popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "char_sleeping_bigzzz", categoryId = "character",
            nameAr = "نوم عميق Zzz", nameEn = "Deep Sleep Zzz",
            drawableRes = R.drawable.img_sleepy_badge_1795028747325,
            accentTint = Color(0xFF6366F1),
            popEffect = BadgePopEffect.ZZZ_BIG
        ),
        FloatingBadgeIconSpec(
            id = "awake", categoryId = "character",
            nameAr = "مستيقظ", nameEn = "Awake",
            drawableRes = R.drawable.badge_state_awake_1795086987920,
            popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "char_awake_spark", categoryId = "character",
            nameAr = "مستيقظ + لمعان", nameEn = "Awake + Sparkle",
            drawableRes = R.drawable.badge_state_awake_1795086987920,
            accentTint = Color(0xFFF59E0B),
            popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "resting", categoryId = "character",
            nameAr = "مسترخي", nameEn = "Resting",
            drawableRes = R.drawable.badge_state_resting_1795086988544,
            popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "char_resting_moon", categoryId = "character",
            nameAr = "راحة + هلال", nameEn = "Resting + Moon",
            drawableRes = R.drawable.badge_state_resting_1795086988544,
            accentTint = Color(0xFF6366F1),
            popEffect = BadgePopEffect.MOON_POP
        ),
        FloatingBadgeIconSpec(
            id = "active", categoryId = "character",
            nameAr = "نشيط", nameEn = "Active",
            drawableRes = R.drawable.badge_state_active_1795086988285,
            popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "char_active_bolt", categoryId = "character",
            nameAr = "نشيط + طاقة", nameEn = "Active + Energy",
            drawableRes = R.drawable.badge_state_active_1795086988285,
            accentTint = Color(0xFFF59E0B),
            popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "happy", categoryId = "character",
            nameAr = "سعيد", nameEn = "Happy",
            drawableRes = R.drawable.badge_state_happy_1795086989074,
            popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "char_happy_heart", categoryId = "character",
            nameAr = "سعيد + قلب", nameEn = "Happy + Heart",
            drawableRes = R.drawable.badge_state_happy_1795086989074,
            accentTint = Color(0xFFEC4899),
            popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "focused", categoryId = "character",
            nameAr = "مُركّز", nameEn = "Focused",
            drawableRes = R.drawable.badge_state_focused_1795087011566,
            accentTint = Color(0xFFF59E0B),
            popEffect = BadgePopEffect.STAR_POP
        ),

        // ==================== 1B. PIXAR 3D MALE CHARACTERS COLLECTION (36) ====================
        // Emotions Pack (9)
        FloatingBadgeIconSpec(
            id = "pixar_guy_happy", categoryId = "pixar_chars",
            nameAr = "سعيد ومبتسم", nameEn = "Happy Smile",
            gridTileUri = "res://img_pixar_chars_emotions_1#0",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_laughing", categoryId = "pixar_chars",
            nameAr = "ضاحك بسعادة", nameEn = "Laughing Guy",
            gridTileUri = "res://img_pixar_chars_emotions_1#1",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_angry", categoryId = "pixar_chars",
            nameAr = "غاضب ومنفعل", nameEn = "Angry Guy",
            gridTileUri = "res://img_pixar_chars_emotions_1#2",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_surprised", categoryId = "pixar_chars",
            nameAr = "مندهش ومتفاجئ", nameEn = "Surprised Guy",
            gridTileUri = "res://img_pixar_chars_emotions_1#3",
            accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_sleepy_cap", categoryId = "pixar_chars",
            nameAr = "نعسان بطاقية نوم", nameEn = "Sleepy Nightcap",
            gridTileUri = "res://img_pixar_chars_emotions_1#4",
            accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_sad", categoryId = "pixar_chars",
            nameAr = "حزين ومتأثر", nameEn = "Sad Guy",
            gridTileUri = "res://img_pixar_chars_emotions_1#5",
            accentTint = Color(0xFF6366F1), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_cool_shades", categoryId = "pixar_chars",
            nameAr = "واثق بنظارة شمس", nameEn = "Confident Shades",
            gridTileUri = "res://img_pixar_chars_emotions_1#6",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_shy_blush", categoryId = "pixar_chars",
            nameAr = "خجول ومبتسم", nameEn = "Shy Blush",
            gridTileUri = "res://img_pixar_chars_emotions_1#7",
            accentTint = Color(0xFFEC4899), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_guy_wink_cap", categoryId = "pixar_chars",
            nameAr = "غامز ومرح", nameEn = "Playful Wink",
            gridTileUri = "res://img_pixar_chars_emotions_1#8",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),

        // Gym & Energetic Fitness Pack (9)
        FloatingBadgeIconSpec(
            id = "pixar_gym_dumbbell", categoryId = "pixar_chars",
            nameAr = "بطل الدمبل", nameEn = "Dumbbell Champ",
            gridTileUri = "res://img_pixar_chars_fitness_1#0",
            accentTint = Color(0xFF2563EB), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_flex", categoryId = "pixar_chars",
            nameAr = "متحمس وعضلات", nameEn = "Flexing Motivated",
            gridTileUri = "res://img_pixar_chars_fitness_1#1",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_runner", categoryId = "pixar_chars",
            nameAr = "عداء كارديو", nameEn = "Cardio Runner",
            gridTileUri = "res://img_pixar_chars_fitness_1#2",
            accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_barbell", categoryId = "pixar_chars",
            nameAr = "رفع البار الثقيل", nameEn = "Barbell Lifter",
            gridTileUri = "res://img_pixar_chars_fitness_1#3",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_victory", categoryId = "pixar_chars",
            nameAr = "محتفل بالفوز", nameEn = "Victory Celebrate",
            gridTileUri = "res://img_pixar_chars_fitness_1#4",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_shaker", categoryId = "pixar_chars",
            nameAr = "شيكر البروتين", nameEn = "Protein Shaker",
            gridTileUri = "res://img_pixar_chars_fitness_1#5",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_focused", categoryId = "pixar_chars",
            nameAr = "تركيز التمرين", nameEn = "Focused Athlete",
            gridTileUri = "res://img_pixar_chars_fitness_1#6",
            accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_kettlebell", categoryId = "pixar_chars",
            nameAr = "تمرين كيتل بل", nameEn = "Kettlebell Guy",
            gridTileUri = "res://img_pixar_chars_fitness_1#7",
            accentTint = Color(0xFF0D9488), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_gym_towel_chill", categoryId = "pixar_chars",
            nameAr = "راحة بعد التمرين", nameEn = "Post-Workout Chill",
            gridTileUri = "res://img_pixar_chars_fitness_1#8",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.CHECK_POP
        ),

        // Daily Lifestyle & Vibes Pack (9)
        FloatingBadgeIconSpec(
            id = "pixar_vibe_coffee", categoryId = "pixar_chars",
            nameAr = "مزاج القهوة", nameEn = "Coffee Lover",
            gridTileUri = "res://img_pixar_chars_vibes_1#0",
            accentTint = Color(0xFFB45309), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_glasses", categoryId = "pixar_chars",
            nameAr = "ذكي ومركز", nameEn = "Smart Glasses",
            gridTileUri = "res://img_pixar_chars_vibes_1#1",
            accentTint = Color(0xFF2563EB), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_headphones", categoryId = "pixar_chars",
            nameAr = "رايق بسماعة", nameEn = "Music Headphones",
            gridTileUri = "res://img_pixar_chars_vibes_1#2",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NOTE_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_party", categoryId = "pixar_chars",
            nameAr = "احتفال وبهجة", nameEn = "Party Confetti",
            gridTileUri = "res://img_pixar_chars_vibes_1#3",
            accentTint = Color(0xFFEC4899), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_thumbsup", categoryId = "pixar_chars",
            nameAr = "ممتاز وبطل", nameEn = "Thumbs Up Pro",
            gridTileUri = "res://img_pixar_chars_vibes_1#4",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_zen", categoryId = "pixar_chars",
            nameAr = "هدوء وتأمل", nameEn = "Peaceful Zen",
            gridTileUri = "res://img_pixar_chars_vibes_1#5",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_apple", categoryId = "pixar_chars",
            nameAr = "أكل صحي وتفاحة", nameEn = "Healthy Apple",
            gridTileUri = "res://img_pixar_chars_vibes_1#6",
            accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_hoodie", categoryId = "pixar_chars",
            nameAr = "روقان الهودي", nameEn = "Cozy Hoodie",
            gridTileUri = "res://img_pixar_chars_vibes_1#7",
            accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_vibe_starry", categoryId = "pixar_chars",
            nameAr = "طاقة وإلهام", nameEn = "Starry Motivated",
            gridTileUri = "res://img_pixar_chars_vibes_1#8",
            accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),

        // Expressive Poses & Moods Pack (9)
        FloatingBadgeIconSpec(
            id = "pixar_pose_cheering", categoryId = "pixar_chars",
            nameAr = "متحمس بشدة", nameEn = "Super Excited",
            gridTileUri = "res://img_pixar_chars_poses_1#0",
            accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_tired", categoryId = "pixar_chars",
            nameAr = "تعبان ومحتاج راحة", nameEn = "Tired Exhausted",
            gridTileUri = "res://img_pixar_chars_poses_1#1",
            accentTint = Color(0xFF6366F1), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_grumpy", categoryId = "pixar_chars",
            nameAr = "متضايق ومكشر", nameEn = "Grumpy Pout",
            gridTileUri = "res://img_pixar_chars_poses_1#2",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_shocked", categoryId = "pixar_chars",
            nameAr = "مصدوم ومندهش", nameEn = "Shocked Face",
            gridTileUri = "res://img_pixar_chars_poses_1#3",
            accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_beard_smile", categoryId = "pixar_chars",
            nameAr = "بشوش بلحية", nameEn = "Cheerful Beard",
            gridTileUri = "res://img_pixar_chars_poses_1#4",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_backcap", categoryId = "pixar_chars",
            nameAr = "كاب رياضي مقلوب", nameEn = "Sporty Cap",
            gridTileUri = "res://img_pixar_chars_poses_1#5",
            accentTint = Color(0xFFEF4444), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_warrior", categoryId = "pixar_chars",
            nameAr = "محارب بعصابة رأس", nameEn = "Warrior Mode",
            gridTileUri = "res://img_pixar_chars_poses_1#6",
            accentTint = Color(0xFFDC2626), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_cloud_nap", categoryId = "pixar_chars",
            nameAr = "نايم على سحابة", nameEn = "Cloud Pillow Nap",
            gridTileUri = "res://img_pixar_chars_poses_1#7",
            accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "pixar_pose_clapping", categoryId = "pixar_chars",
            nameAr = "يصفق بفرح", nameEn = "Joyful Clapping",
            gridTileUri = "res://img_pixar_chars_poses_1#8",
            accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.STAR_POP
        ),

        // ==================== 2. SLEEP & REST (12) ====================
        FloatingBadgeIconSpec(
            id = "sleep_zzz_cloud", categoryId = "sleep",
            nameAr = "سحابة Zzz بارزة", nameEn = "Pop Zzz Cloud",
            vector = Icons.Rounded.Cloud, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF2563EB), accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.ZZZ_BIG
        ),
        FloatingBadgeIconSpec(
            id = "sleep_moon_zzz", categoryId = "sleep",
            nameAr = "هلال النوم Zzz", nameEn = "Sleepy Moon Zzz",
            vector = Icons.Rounded.Bedtime, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF5B21B6), accentTint = Color(0xFF6366F1), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_bed_cozy", categoryId = "sleep",
            nameAr = "سرير مريح", nameEn = "Cozy Bed",
            vector = Icons.Rounded.Bed, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF0F766E), accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_snooze_alarm", categoryId = "sleep",
            nameAr = "غفوة هادئة", nameEn = "Gentle Snooze",
            vector = Icons.Rounded.Snooze, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_night_stay", categoryId = "sleep",
            nameAr = "سماء الليل", nameEn = "Starry Night",
            vector = Icons.Rounded.NightsStay, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF4338CA), accentTint = Color(0xFF818CF8), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_crescent_glow", categoryId = "sleep",
            nameAr = "قمر هادئ", nameEn = "Calm Crescent",
            vector = Icons.Rounded.NightlightRound, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_hotel_nap", categoryId = "sleep",
            nameAr = "قيلولة الظهيرة", nameEn = "Power Nap",
            vector = Icons.Rounded.Hotel, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFC2410C), accentTint = Color(0xFFFB923C), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_dnd_rest", categoryId = "sleep",
            nameAr = "وقت راحة", nameEn = "Rest Mode",
            vector = Icons.Rounded.DoNotDisturbOn, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF6D28D9), accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.MOON_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_sofa_chill", categoryId = "sleep",
            nameAr = "استراحة كنبة", nameEn = "Lounge Rest",
            vector = Icons.Rounded.Weekend, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF92400E), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "sleep_dream_star", categoryId = "sleep",
            nameAr = "نجمة الأحلام", nameEn = "Dream Star",
            vector = Icons.Rounded.AutoAwesome, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF1D4ED8), accentTint = Color(0xFF60A5FA), popEffect = BadgePopEffect.ZZZ_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_twilight", categoryId = "sleep",
            nameAr = "غروب هادئ", nameEn = "Soft Twilight",
            vector = Icons.Rounded.WbTwilight, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFEA580C), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.MOON_POP
        ),
        FloatingBadgeIconSpec(
            id = "sleep_lullaby", categoryId = "sleep",
            nameAr = "موسيقى نوم", nameEn = "Sleep Lullaby",
            vector = Icons.Rounded.MusicNote, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF7C3AED), accentTint = Color(0xFFA78BFA), popEffect = BadgePopEffect.ZZZ_POP
        ),

        // ==================== 3. AWAKE & ACTIVE (10) ====================
        FloatingBadgeIconSpec(
            id = "active_morning_sun", categoryId = "active",
            nameAr = "شمس الصباح", nameEn = "Morning Sun",
            vector = Icons.Rounded.WbSunny, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFD97706), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_bolt_energy", categoryId = "active",
            nameAr = "طاقة عالية", nameEn = "High Energy",
            vector = Icons.Rounded.Bolt, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFEA580C), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_wakeup_alarm", categoryId = "active",
            nameAr = "منبه النشاط", nameEn = "Wake Up Bell",
            vector = Icons.Rounded.Alarm, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF059669), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_rocket_go", categoryId = "active",
            nameAr = "انطلاق صاروخي", nameEn = "Rocket Boost",
            vector = Icons.Rounded.RocketLaunch, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_streak_flame", categoryId = "active",
            nameAr = "شعلة حماس", nameEn = "Streak Flame",
            vector = Icons.Rounded.LocalFireDepartment, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFDC2626), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_flash_spark", categoryId = "active",
            nameAr = "شرارة نشاط", nameEn = "Power Spark",
            vector = Icons.Rounded.FlashOn, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFEAB308), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_light_mode", categoryId = "active",
            nameAr = "إشراقة يوم", nameEn = "Bright Day",
            vector = Icons.Rounded.LightMode, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFCA8A04), accentTint = Color(0xFFFACC15), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_turbo_speed", categoryId = "active",
            nameAr = "سرعة فائقة", nameEn = "Turbo Mode",
            vector = Icons.Rounded.Speed, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF0D9488), accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_trophy_win", categoryId = "active",
            nameAr = "كأس الإنجاز", nameEn = "Victory Cup",
            vector = Icons.Rounded.EmojiEvents, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "active_celebrate", categoryId = "active",
            nameAr = "احتفال ونشاط", nameEn = "Celebrate Go",
            vector = Icons.Rounded.Celebration, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFDB2777), accentTint = Color(0xFFEC4899), popEffect = BadgePopEffect.SPARKLE_POP
        ),

        // ==================== 4. GYM & WORKOUT (14) ====================
        FloatingBadgeIconSpec(
            id = "gym_dumbbell_mint", categoryId = "gym",
            nameAr = "دمبل الجيم", nameEn = "Gym Dumbbell",
            vector = Icons.Rounded.FitnessCenter, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF0F766E), accentTint = Color(0xFF18C28B), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_dumbbell_fire", categoryId = "gym",
            nameAr = "تمرين ناري", nameEn = "Fire Workout",
            vector = Icons.Rounded.FitnessCenter, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFDC2626), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_gymnastics", categoryId = "gym",
            nameAr = "لياقة ومرونة", nameEn = "Calisthenics",
            vector = Icons.Rounded.SportsGymnastics, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0369A1), accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_boxing_mma", categoryId = "gym",
            nameAr = "ملاكمة وقوة", nameEn = "Boxing Power",
            vector = Icons.Rounded.SportsMma, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFBE123C), accentTint = Color(0xFFF43F5E), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_martial_arts", categoryId = "gym",
            nameAr = "فنون قتالية", nameEn = "Martial Arts",
            vector = Icons.Rounded.SportsMartialArts, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF9A3412), accentTint = Color(0xFFEA580C), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_hiit_timer", categoryId = "gym",
            nameAr = "مؤقت التمرين", nameEn = "Workout Timer",
            vector = Icons.Rounded.Timer, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF047857), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "gym_cardio_heart", categoryId = "gym",
            nameAr = "نبض الكارديو", nameEn = "Cardio Pulse",
            vector = Icons.Rounded.MonitorHeart, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFE11D48), accentTint = Color(0xFFFB7185), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_stretch_body", categoryId = "gym",
            nameAr = "إحماء وتمدد", nameEn = "Warmup Stretch",
            vector = Icons.Rounded.AccessibilityNew, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF15803D), accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_medal_champ", categoryId = "gym",
            nameAr = "ميدالية بطل", nameEn = "Gym Medal",
            vector = Icons.Rounded.MilitaryTech, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_finish_flag", categoryId = "gym",
            nameAr = "نهاية الجولة", nameEn = "Finish Set",
            vector = Icons.Rounded.SportsScore, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF1D4ED8), accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.CHECK_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_leg_day", categoryId = "gym",
            nameAr = "يوم الرجل", nameEn = "Leg Day",
            vector = Icons.Rounded.DirectionsRun, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFC2410C), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.FLAME_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_shaker_hydrate", categoryId = "gym",
            nameAr = "شيكر البروتين", nameEn = "Protein Shake",
            vector = Icons.Rounded.LocalDrink, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.DROP_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_recovery_ice", categoryId = "gym",
            nameAr = "استشفاء عضلي", nameEn = "Muscle Recovery",
            vector = Icons.Rounded.AcUnit, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0369A1), accentTint = Color(0xFF06B6D4), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "gym_champion_badge", categoryId = "gym",
            nameAr = "وسام القوة", nameEn = "Strength Award",
            vector = Icons.Rounded.WorkspacePremium, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFA16207), accentTint = Color(0xFFEAB308), popEffect = BadgePopEffect.STAR_POP
        ),

        // ==================== 5. WALKING & MOVEMENT (10) ====================
        FloatingBadgeIconSpec(
            id = "walk_steps_mint", categoryId = "walk",
            nameAr = "مشي يومي", nameEn = "Daily Walk",
            vector = Icons.Rounded.DirectionsWalk, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF059669), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_run_fast", categoryId = "walk",
            nameAr = "جري سريع", nameEn = "Morning Run",
            vector = Icons.Rounded.DirectionsRun, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_bike_ride", categoryId = "walk",
            nameAr = "ركوب دراجة", nameEn = "Cycling",
            vector = Icons.Rounded.DirectionsBike, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF15803D), accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "walk_hiking", categoryId = "walk",
            nameAr = "تسلق ومشي", nameEn = "Trail Hike",
            vector = Icons.Rounded.Hiking, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF92400E), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_swimming", categoryId = "walk",
            nameAr = "سباحة ومنعش", nameEn = "Swimming",
            vector = Icons.Rounded.Pool, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0369A1), accentTint = Color(0xFF06B6D4), popEffect = BadgePopEffect.DROP_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_yoga_lotus", categoryId = "walk",
            nameAr = "يوجا وتوازن", nameEn = "Yoga Balance",
            vector = Icons.Rounded.SelfImprovement, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF6D28D9), accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_park_stroll", categoryId = "walk",
            nameAr = "نزهة حديقة", nameEn = "Park Stroll",
            vector = Icons.Rounded.Park, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF166534), accentTint = Color(0xFF16A34A), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "walk_mountain", categoryId = "walk",
            nameAr = "قمة الجبل", nameEn = "Mountain Peak",
            vector = Icons.Rounded.Terrain, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF78350F), accentTint = Color(0xFFB45309), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_forest_air", categoryId = "walk",
            nameAr = "هواء الطبيعة", nameEn = "Forest Trail",
            vector = Icons.Rounded.Forest, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF065F46), accentTint = Color(0xFF059669), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "walk_goal_target", categoryId = "walk",
            nameAr = "هدف الخطوات", nameEn = "Step Goal",
            vector = Icons.Rounded.MyLocation, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFEA580C), accentTint = Color(0xFFFB923C), popEffect = BadgePopEffect.CHECK_POP
        ),

        // ==================== 6. FOOD & MEALS (12) ====================
        FloatingBadgeIconSpec(
            id = "food_healthy_meal", categoryId = "food",
            nameAr = "وجبة صحية", nameEn = "Healthy Meal",
            vector = Icons.Rounded.Restaurant, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF047857), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_dining_plate", categoryId = "food",
            nameAr = "وقت الغداء", nameEn = "Lunch Time",
            vector = Icons.Rounded.LocalDining, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "food_warm_soup", categoryId = "food",
            nameAr = "شوربة ساخنة", nameEn = "Warm Bowl",
            vector = Icons.Rounded.RamenDining, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFC2410C), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_ancient_bread", categoryId = "food",
            nameAr = "خبز طازج", nameEn = "Fresh Bread",
            vector = Icons.Rounded.BakeryDining, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF92400E), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_breakfast", categoryId = "food",
            nameAr = "فطار الصباح", nameEn = "Breakfast",
            vector = Icons.Rounded.BreakfastDining, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFCA8A04), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_protein_egg", categoryId = "food",
            nameAr = "بروتين وبيض", nameEn = "Protein Egg",
            vector = Icons.Rounded.Egg, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFFBBF24), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "food_lunch_box", categoryId = "food",
            nameAr = "ساندوتش لذيذ", nameEn = "Tasty Bite",
            vector = Icons.Rounded.LunchDining, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFDC2626), accentTint = Color(0xFFF97316), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "food_grains_wheat", categoryId = "food",
            nameAr = "قمح وشعير", nameEn = "Whole Grains",
            vector = Icons.Rounded.Grain, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF854D0E), accentTint = Color(0xFFCA8A04), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_green_salad", categoryId = "food",
            nameAr = "سلطة وخضار", nameEn = "Fresh Veggies",
            vector = Icons.Rounded.Eco, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF15803D), accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_sweet_cookie", categoryId = "food",
            nameAr = "سناك خفيف", nameEn = "Sweet Snack",
            vector = Icons.Rounded.Cookie, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF9A3412), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_icecream", categoryId = "food",
            nameAr = "تحلية باردة", nameEn = "Sweet Treat",
            vector = Icons.Rounded.Icecream, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFDB2777), accentTint = Color(0xFFF472B6), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "food_fruit_bloom", categoryId = "food",
            nameAr = "فواكه طبيعية", nameEn = "Fresh Fruits",
            vector = Icons.Rounded.LocalFlorist, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFBE123C), accentTint = Color(0xFFFB7185), popEffect = BadgePopEffect.HEART_POP
        ),

        // ==================== 7. COFFEE & DRINKS (10) ====================
        FloatingBadgeIconSpec(
            id = "drink_coffee_steam", categoryId = "coffee",
            nameAr = "قهوة ساخنة", nameEn = "Hot Coffee",
            vector = Icons.Rounded.Coffee, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF78350F), accentTint = Color(0xFFB45309), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_cafe_cup", categoryId = "coffee",
            nameAr = "فنجان مزاج", nameEn = "Cafe Cup",
            vector = Icons.Rounded.LocalCafe, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFF92400E), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_mint_tea", categoryId = "coffee",
            nameAr = "شاي بالنعناع", nameEn = "Mint Tea",
            vector = Icons.Rounded.EmojiFoodBeverage, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF047857), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_water_drop", categoryId = "coffee",
            nameAr = "شرب الماء", nameEn = "Drink Water",
            vector = Icons.Rounded.WaterDrop, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.DROP_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_fresh_juice", categoryId = "coffee",
            nameAr = "عصير طازج", nameEn = "Fresh Juice",
            vector = Icons.Rounded.LocalDrink, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFEA580C), accentTint = Color(0xFFFB923C), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_morning_mug", categoryId = "coffee",
            nameAr = "مج الصباح", nameEn = "Morning Mug",
            vector = Icons.Rounded.FreeBreakfast, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF7C2D12), accentTint = Color(0xFFEA580C), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_espresso_bolt", categoryId = "coffee",
            nameAr = "إسبريسو طاقة", nameEn = "Espresso Shot",
            vector = Icons.Rounded.Coffee, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFF451A03), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_iced_mint", categoryId = "coffee",
            nameAr = "مشروب مثلج", nameEn = "Iced Drink",
            vector = Icons.Rounded.AcUnit, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0369A1), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.DROP_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_herbal_calm", categoryId = "coffee",
            nameAr = "مشروب أعشاب", nameEn = "Herbal Tea",
            vector = Icons.Rounded.EmojiFoodBeverage, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF15803D), accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "drink_hydration_check", categoryId = "coffee",
            nameAr = "هدف الترطيب", nameEn = "Hydration Goal",
            vector = Icons.Rounded.WaterDrop, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF1D4ED8), accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.CHECK_POP
        ),

        // ==================== 8. RELAXATION & CALM (10) ====================
        FloatingBadgeIconSpec(
            id = "relax_spa_lotus", categoryId = "relax",
            nameAr = "استرخاء وهدوء", nameEn = "Spa Lotus",
            vector = Icons.Rounded.Spa, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF0F766E), accentTint = Color(0xFF14B8A6), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_zen_cloud", categoryId = "relax",
            nameAr = "سحابة الصفاء", nameEn = "Zen Cloud",
            vector = Icons.Rounded.Cloud, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_chill_music", categoryId = "relax",
            nameAr = "سماعة روقان", nameEn = "Chill Audio",
            vector = Icons.Rounded.Headphones, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF6D28D9), accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.NOTE_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_breeze_air", categoryId = "relax",
            nameAr = "تنفس عميق", nameEn = "Deep Breath",
            vector = Icons.Rounded.Air, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF0369A1), accentTint = Color(0xFF0EA5E9), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "relax_warm_bath", categoryId = "relax",
            nameAr = "استحمام دافئ", nameEn = "Warm Bath",
            vector = Icons.Rounded.Bathtub, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0E7490), accentTint = Color(0xFF06B6D4), popEffect = BadgePopEffect.STEAM_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_nature_leaf", categoryId = "relax",
            nameAr = "ورقة هدوء", nameEn = "Calm Leaf",
            vector = Icons.Rounded.Eco, bgTop = sageTop, bgBottom = sageBot,
            iconTint = Color(0xFF166534), accentTint = Color(0xFF22C55E), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "relax_flower_peace", categoryId = "relax",
            nameAr = "زهرة ناعمة", nameEn = "Soft Blossom",
            vector = Icons.Rounded.FilterVintage, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFDB2777), accentTint = Color(0xFFF472B6), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_butterfly", categoryId = "relax",
            nameAr = "فراشة هادئة", nameEn = "Gentle Nature",
            vector = Icons.Rounded.EmojiNature, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF059669), accentTint = Color(0xFF34D399), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_meditate", categoryId = "relax",
            nameAr = "تأمل وراحة", nameEn = "Meditation",
            vector = Icons.Rounded.SelfImprovement, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.MOON_POP
        ),
        FloatingBadgeIconSpec(
            id = "relax_rain_calm", categoryId = "relax",
            nameAr = "مطر هادئ", nameEn = "Soft Rain",
            vector = Icons.Rounded.WaterDrop, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF4F46E5), accentTint = Color(0xFF818CF8), popEffect = BadgePopEffect.DROP_POP
        ),

        // ==================== 9. WORK & STUDY (10) ====================
        FloatingBadgeIconSpec(
            id = "work_study_book", categoryId = "work",
            nameAr = "قراءة ومذاكرة", nameEn = "Study Book",
            vector = Icons.Rounded.MenuBook, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF1D4ED8), accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_pencil_note", categoryId = "work",
            nameAr = "كتابة وملاحظات", nameEn = "Quick Notes",
            vector = Icons.Rounded.Edit, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFB45309), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "work_laptop_focus", categoryId = "work",
            nameAr = "لابتوب وعمل", nameEn = "Laptop Focus",
            vector = Icons.Rounded.LaptopMac, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0F172A), accentTint = Color(0xFF0284C7), popEffect = BadgePopEffect.BOLT_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_idea_bulb", categoryId = "work",
            nameAr = "فكرة ذكية", nameEn = "Bright Idea",
            vector = Icons.Rounded.Lightbulb, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFCA8A04), accentTint = Color(0xFFFACC15), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_goal_target", categoryId = "work",
            nameAr = "هدف اليوم", nameEn = "Daily Target",
            vector = Icons.Rounded.MyLocation, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFBE123C), accentTint = Color(0xFFF43F5E), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_calendar", categoryId = "work",
            nameAr = "جدول ومواعيد", nameEn = "Schedule",
            vector = Icons.Rounded.CalendarMonth, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF0F766E), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "work_done_task", categoryId = "work",
            nameAr = "مهمة منجزة", nameEn = "Task Done",
            vector = Icons.Rounded.TaskAlt, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF059669), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.CHECK_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_grad_cap", categoryId = "work",
            nameAr = "دراسة وتفوق", nameEn = "Study Success",
            vector = Icons.Rounded.School, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF5B21B6), accentTint = Color(0xFF8B5CF6), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "work_hourglass", categoryId = "work",
            nameAr = "وقت التركيز", nameEn = "Focus Timer",
            vector = Icons.Rounded.HourglassTop, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF92400E), accentTint = Color(0xFFD97706), popEffect = BadgePopEffect.NONE
        ),
        FloatingBadgeIconSpec(
            id = "work_briefcase", categoryId = "work",
            nameAr = "شغل ومكتب", nameEn = "Work Mode",
            vector = Icons.Rounded.Work, bgTop = skyTop, bgBottom = skyBot,
            iconTint = Color(0xFF1E3A8A), accentTint = Color(0xFF3B82F6), popEffect = BadgePopEffect.CHECK_POP
        ),

        // ==================== 10. MOODS & CUTE SYMBOLS (10) ====================
        FloatingBadgeIconSpec(
            id = "mood_pink_heart", categoryId = "mood",
            nameAr = "قلب لطيف", nameEn = "Cute Heart",
            vector = Icons.Rounded.Favorite, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFE11D48), accentTint = Color(0xFFFB7185), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_happy_smile", categoryId = "mood",
            nameAr = "ابتسامة سعيدة", nameEn = "Happy Smile",
            vector = Icons.Rounded.SentimentVerySatisfied, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFD97706), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_golden_star", categoryId = "mood",
            nameAr = "نجمة ذهبية", nameEn = "Golden Star",
            vector = Icons.Rounded.Star, bgTop = creamTop, bgBottom = creamBot,
            iconTint = Color(0xFFCA8A04), accentTint = Color(0xFFFACC15), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_diamond", categoryId = "mood",
            nameAr = "ماسة لامعة", nameEn = "Sparkle Gem",
            vector = Icons.Rounded.Diamond, bgTop = iceTop, bgBottom = iceBot,
            iconTint = Color(0xFF0284C7), accentTint = Color(0xFF38BDF8), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_rainbow", categoryId = "mood",
            nameAr = "قوس قزح", nameEn = "Pastel Rainbow",
            vector = Icons.Rounded.Looks, bgTop = peachTop, bgBottom = peachBot,
            iconTint = Color(0xFFEC4899), accentTint = Color(0xFFF59E0B), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_gift_box", categoryId = "mood",
            nameAr = "هدية ومكافأة", nameEn = "Reward Gift",
            vector = Icons.Rounded.CardGiftcard, bgTop = roseTop, bgBottom = roseBot,
            iconTint = Color(0xFFDB2777), accentTint = Color(0xFFF472B6), popEffect = BadgePopEffect.STAR_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_melody", categoryId = "mood",
            nameAr = "نغمة لطيفة", nameEn = "Cute Melody",
            vector = Icons.Rounded.MusicNote, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF7C3AED), accentTint = Color(0xFFA78BFA), popEffect = BadgePopEffect.NOTE_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_cute_paw", categoryId = "mood",
            nameAr = "أثر لطيف", nameEn = "Cute Paw",
            vector = Icons.Rounded.Pets, bgTop = sandTop, bgBottom = sandBot,
            iconTint = Color(0xFF9A3412), accentTint = Color(0xFFEA580C), popEffect = BadgePopEffect.HEART_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_magic_wand", categoryId = "mood",
            nameAr = "لمسة سحرية", nameEn = "Magic Touch",
            vector = Icons.Rounded.AutoFixHigh, bgTop = lavTop, bgBottom = lavBot,
            iconTint = Color(0xFF6D28D9), accentTint = Color(0xFFC084FC), popEffect = BadgePopEffect.SPARKLE_POP
        ),
        FloatingBadgeIconSpec(
            id = "mood_verified_mint", categoryId = "mood",
            nameAr = "وسام مميز", nameEn = "Special Badge",
            vector = Icons.Rounded.Verified, bgTop = mintTop, bgBottom = mintBot,
            iconTint = Color(0xFF059669), accentTint = Color(0xFF10B981), popEffect = BadgePopEffect.CHECK_POP
        )
    )

    fun byId(id: String): FloatingBadgeIconSpec =
        icons.firstOrNull { it.id == id } ?: icons.first()

    fun filter(categoryId: String, query: String): List<FloatingBadgeIconSpec> {
        val q = query.trim().lowercase()
        return icons.filter { spec ->
            val catMatch = categoryId == "all" || spec.categoryId == categoryId
            val textMatch = q.isEmpty() ||
                spec.nameAr.lowercase().contains(q) ||
                spec.nameEn.lowercase().contains(q) ||
                spec.id.lowercase().contains(q)
            catMatch && textMatch
        }
    }
}
