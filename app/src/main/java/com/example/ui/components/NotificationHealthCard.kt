package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.notification.TaskAlarmScheduler
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private tailrec fun android.content.Context.findLifecycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    is android.content.ContextWrapper -> baseContext.findLifecycleOwner()
    else -> null
}

/**
 * Interactive delivery-health & test panel for native background and lock-screen notifications.
 * Lives inside Settings -> Alerts without altering the main screen layout.
 */
@Composable
fun BackgroundNotificationsHealthCard(
    lang: String,
    onScheduleTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hostLifecycleOwner = remember(context) { context.findLifecycleOwner() }
    val windowInfo = LocalWindowInfo.current
    val colors = LocalMNotesColors.current

    // Re-check live permission & battery status on Activity resume, sheet lifecycle resume,
    // window focus restoration (returning from OEM Settings overlay), and lightweight live tick.
    var refreshTick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner, hostLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                refreshTick++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (hostLifecycleOwner != null && hostLifecycleOwner !== lifecycleOwner) {
            hostLifecycleOwner.lifecycle.addObserver(observer)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (hostLifecycleOwner != null && hostLifecycleOwner !== lifecycleOwner) {
                hostLifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            refreshTick++
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1200L)
            refreshTick++
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTick++
    }

    val hasNotifPerm = remember(refreshTick) { TaskAlarmScheduler.hasNotificationPermission(context) }
    val hasExactAlarm = remember(refreshTick) { TaskAlarmScheduler.canScheduleExactAlarms(context) }
    val ignoringBattery = remember(refreshTick) { TaskAlarmScheduler.isIgnoringBatteryOptimizations(context) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.bgMain,
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .testTag("background_notifications_health_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") {
                            "Background & Lock-Screen Reminders"
                        } else {
                            "إشعارات الخلفية وقفل الشاشة"
                        },
                        color = colors.textDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (lang == "en") {
                            "Ensures alarms deliver on time even when the app is closed or screen is locked."
                        } else {
                            "يضمن وصول التذكيرات بدقة حتى عند إغلاق التطبيق أو قفل الشاشة أو إعادة تشغيل الهاتف."
                        },
                        color = colors.textMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            HealthStatusRow(
                icon = Icons.Rounded.NotificationsActive,
                title = if (lang == "en") "Notifications Permission" else "إذن الإشعارات",
                subtitle = if (hasNotifPerm) {
                    if (lang == "en") "Allowed (Lock-screen visible)" else "مفعّل (تظهر على شاشة القفل)"
                } else {
                    if (lang == "en") "Required for alerts to appear" else "مطلوب لظهور الإشعارات"
                },
                isOk = hasNotifPerm,
                actionLabel = if (hasNotifPerm) {
                    if (lang == "en") "Settings" else "الإعدادات"
                } else {
                    if (lang == "en") "Allow" else "تفعيل"
                },
                testTag = "notif_health_permission_btn",
                onAction = {
                    if (!hasNotifPerm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        TaskAlarmScheduler.openNotificationSettings(context)
                    }
                }
            )

            HealthStatusRow(
                icon = Icons.Rounded.Alarm,
                title = if (lang == "en") "Exact Alarms (On-Time)" else "المنبّهات الدقيقة (في الموعد بالضبط)",
                subtitle = if (hasExactAlarm) {
                    if (lang == "en") "Enabled (Wakes from Doze)" else "مفعّل (يعمل حتى أثناء السكون)"
                } else {
                    if (lang == "en") "Allow exact timing so alerts are never delayed" else "فعّله حتى لا يتأخر أي تنبيه"
                },
                isOk = hasExactAlarm,
                actionLabel = if (hasExactAlarm) {
                    if (lang == "en") "Active" else "مفعّل"
                } else {
                    if (lang == "en") "Enable" else "تفعيل"
                },
                testTag = "notif_health_exact_alarm_btn",
                onAction = { TaskAlarmScheduler.openExactAlarmSettings(context) }
            )

            HealthStatusRow(
                icon = Icons.Rounded.BatteryChargingFull,
                title = if (lang == "en") "Unrestricted Battery" else "استثناء توفير البطارية",
                subtitle = if (ignoringBattery) {
                    if (lang == "en") "Unrestricted (Protected from kill)" else "غير مقيد (محمي من الإيقاف التلقائي)"
                } else {
                    if (lang == "en") "Recommended so Android doesn't pause alarms" else "موصى به حتى لا يوقف النظام التذكيرات"
                },
                isOk = ignoringBattery,
                actionLabel = if (ignoringBattery) {
                    if (lang == "en") "Done" else "ممتاز"
                } else {
                    if (lang == "en") "Open" else "ضبط"
                },
                testTag = "notif_health_battery_btn",
                onAction = { TaskAlarmScheduler.openBatteryOptimizationSettings(context) }
            )

            Spacer(Modifier.height(2.dp))

            Button(
                onClick = {
                    if (!hasNotifPerm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onScheduleTestNotification()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_lockscreen_notification_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (lang == "en") {
                        "Test Lock-Screen Notification (5s)"
                    } else {
                        "تجربة إشعار قفل الشاشة بعد ٥ ثوانٍ"
                    },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun HealthStatusRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isOk: Boolean,
    actionLabel: String,
    testTag: String,
    onAction: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val okGreen = Color(0xFF16A34A)
    val warnAmber = Color(0xFFD97706)
    val statusColor = if (isOk) okGreen else warnAmber

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isOk) okGreen.copy(alpha = 0.25f) else warnAmber.copy(alpha = 0.45f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isOk) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isOk) colors.primarySoft else statusColor.copy(alpha = 0.14f))
                    .border(
                        1.dp,
                        if (isOk) colors.primary.copy(alpha = 0.35f) else statusColor.copy(alpha = 0.55f),
                        RoundedCornerShape(999.dp)
                    )
                    .clickable(onClick = onAction)
                    .padding(horizontal = 11.dp, vertical = 6.dp)
                    .testTag(testTag)
            ) {
                Text(
                    text = actionLabel,
                    color = if (isOk) colors.primary else statusColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
