package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.LibraryPhoto
import com.example.model.MNotesCatalog
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.TrimmedRingtone
import com.example.ui.theme.LocalMNotesColors
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorSheet(
    editingTask: SmartTask?,
    draftImgUri: String,
    lang: String,
    libraryPhotos: List<LibraryPhoto>,
    trimmedRingtones: List<TrimmedRingtone>,
    onPreviewRingtone: (String) -> Unit,
    onPickAndCropPhoto: (Uri) -> Unit,
    onSelectDraftImg: (String) -> Unit,
    onSave: (
        title: String,
        meta: String,
        startDate: String,
        alertTime: String,
        repeatMode: RepeatMode,
        cycleStepsRaw: String,
        weekDays: List<Long>,
        alertEnabled: Boolean,
        imgUri: String,
        ringId: String,
        testAlertAfterSave: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(editingTask?.id) { mutableStateOf(editingTask?.title ?: "") }
    var meta by remember(editingTask?.id) { mutableStateOf(editingTask?.meta ?: "") }
    var startDate by remember(editingTask?.id) {
        mutableStateOf(editingTask?.nextDueDate?.ifBlank { AppStrings.todayStr() } ?: AppStrings.todayStr())
    }
    var alertTime by remember(editingTask?.id) { mutableStateOf(editingTask?.alertTime ?: "") }
    var repeatMode by remember(editingTask?.id) { mutableStateOf(editingTask?.repeat ?: RepeatMode.NONE) }
    var cycleStepsRaw by remember(editingTask?.id) {
        mutableStateOf(
            if (editingTask != null && editingTask.cycleSteps.isNotEmpty()) {
                editingTask.cycleSteps.joinToString("، ")
            } else {
                AppStrings.get(lang, "cycleDefault")
            }
        )
    }
    var selectedDays by remember(editingTask?.id) { mutableStateOf(editingTask?.weekDays ?: emptyList()) }
    var alertEnabled by remember(editingTask?.id) { mutableStateOf(editingTask?.alertEnabled ?: true) }
    var selectedRingId by remember(editingTask?.id) { mutableStateOf(editingTask?.ringId ?: "") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickAndCropPhoto(uri)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get(lang, if (editingTask != null) "editTask" else "newTask"),
                    color = colors.textDark,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(AppStrings.get(lang, "close"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    FieldLabel(AppStrings.get(lang, "taskName"))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderSoft,
                            focusedContainerColor = colors.bgMain,
                            unfocusedContainerColor = colors.bgMain
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    FieldLabel(AppStrings.get(lang, "taskDesc"))
                    OutlinedTextField(
                        value = meta,
                        onValueChange = { meta = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderSoft,
                            focusedContainerColor = colors.bgMain,
                            unfocusedContainerColor = colors.bgMain
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_meta_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel(AppStrings.get(lang, "startDate"))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.bgMain,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val parsed = runCatching { LocalDate.parse(startDate) }.getOrElse { LocalDate.now() }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                            },
                                            parsed.year,
                                            parsed.monthValue - 1,
                                            parsed.dayOfMonth
                                        ).show()
                                    }
                                    .testTag("task_date_picker")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = startDate,
                                        color = colors.textDark,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel(AppStrings.get(lang, "alertTime"))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.bgMain,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val parts = alertTime.split(":")
                                        val cal = Calendar.getInstance()
                                        val initH = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY)
                                        val initM = parts.getOrNull(1)?.toIntOrNull() ?: cal.get(Calendar.MINUTE)
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                alertTime = "%02d:%02d".format(h, m)
                                            },
                                            initH,
                                            initM,
                                            false
                                        ).show()
                                    }
                                    .testTag("task_time_picker")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (alertTime.isNotBlank()) AppStrings.formatTime(alertTime, lang)
                                        else AppStrings.get(lang, "clearTime"),
                                        color = colors.textDark,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    FieldLabel(AppStrings.get(lang, "repeat"))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val repOptions = listOf(
                            RepeatMode.NONE to AppStrings.get(lang, "repNone"),
                            RepeatMode.DAILY to AppStrings.get(lang, "repDaily"),
                            RepeatMode.WEEKLY to AppStrings.get(lang, "repWeekly"),
                            RepeatMode.CYCLE to AppStrings.get(lang, "repCycle")
                        )
                        repOptions.forEach { (mode, label) ->
                            val active = repeatMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (active) colors.primary else colors.bgMain)
                                    .border(
                                        1.dp,
                                        if (active) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable { repeatMode = mode }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) Color.White else colors.textMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (repeatMode == RepeatMode.CYCLE) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.primarySoft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = AppStrings.get(lang, "cycleLbl"),
                                    color = colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = cycleStepsRaw,
                                    onValueChange = { cycleStepsRaw = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.borderSoft,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Always show circular weekday selection buttons inside Task Editor
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.primarySoft.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == "en") "Scheduled Weekdays (repeats forever)" else "أيام التمرين أو المهمة (تتكرر للأبد)",
                                    color = colors.primary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.weight(1f)
                                )
                                val allSevenSelected = selectedDays.size == 7
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (allSevenSelected) colors.primary else colors.cardBg)
                                        .border(
                                            1.dp,
                                            if (allSevenSelected) colors.primary else colors.borderSoft,
                                            RoundedCornerShape(999.dp)
                                        )
                                        .clickable {
                                            if (allSevenSelected) {
                                                selectedDays = emptyList()
                                            } else {
                                                selectedDays = (0L..6L).toList()
                                                if (repeatMode == RepeatMode.NONE) {
                                                    repeatMode = RepeatMode.WEEKLY
                                                }
                                            }
                                        }
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") "Every day" else "كل الأيام",
                                        color = if (allSevenSelected) Color.White else colors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AppStrings.dayNames(lang).forEachIndexed { idx, dayName ->
                                    val dLong = idx.toLong()
                                    val active = selectedDays.contains(dLong)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (active) colors.primary else colors.cardBg)
                                            .border(
                                                1.5.dp,
                                                if (active) colors.primary else colors.borderSoft,
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedDays = if (active) {
                                                    selectedDays - dLong
                                                } else {
                                                    if (repeatMode == RepeatMode.NONE) {
                                                        repeatMode = RepeatMode.WEEKLY
                                                    }
                                                    selectedDays + dLong
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayName,
                                            color = if (active) Color.White else colors.textDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            if (selectedDays.isNotEmpty() && selectedDays.size < 7) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (lang == "en") {
                                        "On off-days this task stays visible with a sleepy Zzz badge."
                                    } else {
                                        "في أيام الراحة تبقى المهمة ظاهرة بعلامة النوم Zzz وتنشط في الأيام المحددة تلقائياً."
                                    },
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (alertEnabled) colors.primarySoft else colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (alertEnabled) colors.primary else colors.borderSoft,
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = AppStrings.get(lang, "enAlert"),
                                color = colors.textDark,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = alertEnabled,
                                onCheckedChange = { alertEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colors.primary,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                    }

                    // Per-task Ringtone & Gym Track selector inside Task Details
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.primarySoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = if (lang == "en") "Task Ringtone / Gym Track" else "نغمة التنبيه والجيم لهذه المهمة",
                                        color = colors.textDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.primarySoft)
                                        .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape)
                                        .clickable { onPreviewRingtone(selectedRingId) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = AppStrings.get(lang, "play"),
                                            tint = colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = AppStrings.get(lang, "tryNow").replace("▶", "").trim(),
                                            color = colors.primary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val defaultActive = selectedRingId.isBlank()
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (defaultActive) colors.primary else colors.cardBg)
                                        .border(
                                            1.dp,
                                            if (defaultActive) colors.primary else colors.borderSoft,
                                            CircleShape
                                        )
                                        .clickable { selectedRingId = "" }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") "Default" else "النغمة العامة",
                                        color = if (defaultActive) Color.White else colors.textDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                MNotesCatalog.gymTracks.forEach { track ->
                                    val active = selectedRingId == track.id
                                    val lbl = (if (lang == "en") track.nameEn else track.nameAr)
                                        .replace(" (١٠ ث)", "")
                                        .replace(" (10s)", "")
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (active) colors.primary else colors.cardBg)
                                            .border(
                                                1.dp,
                                                if (active) colors.primary else colors.borderSoft,
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedRingId = track.id
                                                onPreviewRingtone(track.id)
                                            }
                                            .padding(horizontal = 11.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = lbl,
                                            color = if (active) Color.White else colors.textMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                trimmedRingtones.forEach { clip ->
                                    val active = selectedRingId == clip.id
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (active) colors.primary else colors.cardBg)
                                            .border(
                                                1.dp,
                                                if (active) colors.primary else colors.borderSoft,
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedRingId = clip.id
                                                onPreviewRingtone(clip.id)
                                            }
                                            .padding(horizontal = 11.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = clip.name,
                                            color = if (active) Color.White else colors.textMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primarySoft)
                            .border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                            .clickable {
                                photoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (draftImgUri.isNotBlank()) AppStrings.get(lang, "photoSelected")
                                else "+ ${AppStrings.get(lang, "pickPhoto")}",
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (lang == "en") "Categorized Photo Folders" else "مجلدات الصور (فواكه • خضروات • جيم • شخصيات)",
                        color = colors.primary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
                    CategorizedBuiltInPhotosBrowser(
                        lang = lang,
                        selectedUri = draftImgUri,
                        columns = 3,
                        defaultCategory = "gym",
                        showLabels = true,
                        onSelectUri = onSelectDraftImg
                    )

                    val enabledLibrary = libraryPhotos.filter { it.enabled }
                    if (enabledLibrary.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            enabledLibrary.take(4).forEach { item ->
                                val selected = draftImgUri == item.filePath
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(if (selected) colors.primarySoft else colors.bgMain)
                                        .border(
                                            if (selected) 2.dp else 1.5.dp,
                                            if (selected) colors.primary else colors.borderSoft,
                                            CircleShape
                                        )
                                        .clickable { onSelectDraftImg(item.filePath) }
                                        .padding(3.dp)
                                ) {
                                    MNotesPhotoContent(
                                        imgUri = item.filePath,
                                        useBannerDefaultIfBlank = false,
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSave(
                                title,
                                meta,
                                startDate,
                                alertTime,
                                repeatMode,
                                cycleStepsRaw,
                                selectedDays,
                                alertEnabled,
                                draftImgUri,
                                selectedRingId,
                                false
                            )
                        },
                        enabled = title.trim().isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("task_save_button")
                    ) {
                        Text(
                            text = AppStrings.get(lang, "saveAll"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    if (editingTask != null) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                onSave(
                                    title,
                                    meta,
                                    startDate,
                                    alertTime,
                                    repeatMode,
                                    cycleStepsRaw,
                                    selectedDays,
                                    alertEnabled,
                                    draftImgUri,
                                    selectedRingId,
                                    true
                                )
                            },
                            enabled = title.trim().isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("task_preview_alert_button")
                        ) {
                            Text(
                                text = AppStrings.get(lang, "testAlert"),
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    val colors = LocalMNotesColors.current
    Text(
        text = text,
        color = colors.textMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}
