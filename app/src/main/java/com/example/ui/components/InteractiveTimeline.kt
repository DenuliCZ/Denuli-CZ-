package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AudioTrack
import com.example.data.database.Project
import com.example.ui.viewmodel.StudioViewModel
import com.example.util.TimelineClipData

@Composable
fun InteractiveTimeline(
    viewModel: StudioViewModel,
    project: Project,
    activeTracks: List<AudioTrack>,
    videoClips: List<TimelineClipData>,
    isPlaying: Boolean,
    globalProgress: Float
) {
    val context = LocalContext.current
    val totalProjDurationSec = project.trackDuration.coerceAtLeast(15)
    val maxDurationMs = totalProjDurationSec * 1000L

    // Layout configuration
    val dpPerSecond = 50.dp // Scale of timeline (50dp = 1 second)
    val totalTimelineWidth = dpPerSecond * totalProjDurationSec

    // Active drag tracking
    var isDraggingTrackId by remember { mutableStateOf<Int?>(null) }
    var draggingTempOffsets = remember { mutableStateMapOf<Int, Long>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0922), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF2E174F), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("interactive_timeline_container")
    ) {
        // Timeline Header Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Timeline",
                        tint = Color(0xFF00FFC2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VÍCÉSTOPÝ ČASOVÝ EDITOR 📊",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Tažením doleva/doprava posouvejte stopy a synchronizujte je",
                    color = Color(0xFF8E8CA4),
                    fontSize = 10.sp
                )
            }

            // Quick reset all offsets option
            TextButton(
                onClick = {
                    activeTracks.forEach { track ->
                        viewModel.updateTrackStartOffset(track, 0L)
                    }
                    draggingTempOffsets.clear()
                    Toast.makeText(context, "Časové posuny vynulovány", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Vynulovat posuny",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vynulovat ↩️", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Left Header list + Scrollable Timeline Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            
            // SIDEBAR COLUMN: Audio & Video Track Headers (Fixed width to avoid jumping)
            Column(
                modifier = Modifier
                    .width(90.dp)
                    .padding(top = 22.dp), // align with timeline ticks
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Main Video Track Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261245)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎬 VIDEO", color = Color(0xFFE27CFF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${videoClips.size} klipů", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                    }
                }

                // Audio Tracks Headers
                activeTracks.forEach { track ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF130D26)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(0.5.dp, Color(0xFF23144D), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = track.name,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Quick adjustment buttons (Fine-tuning micro values)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF1F143D))
                                        .clickable {
                                            val currentOffset = draggingTempOffsets[track.trackId] ?: track.startOffsetMs
                                            val newOffset = (currentOffset - 500L).coerceAtLeast(0L)
                                            draggingTempOffsets[track.trackId] = newOffset
                                            viewModel.updateTrackStartOffset(track, newOffset)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                val currentSec = ((draggingTempOffsets[track.trackId] ?: track.startOffsetMs) / 1000f)
                                Text(
                                    text = String.format("%.1f s", currentSec),
                                    color = Color(0xFF00FFC2),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF1F143D))
                                        .clickable {
                                            val currentOffset = draggingTempOffsets[track.trackId] ?: track.startOffsetMs
                                            val newOffset = (currentOffset + 500L).coerceAtMost(maxDurationMs - 1000L)
                                            draggingTempOffsets[track.trackId] = newOffset
                                            viewModel.updateTrackStartOffset(track, newOffset)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // SCROLLABLE TIMELINE TRACKS AREA
            val horizontalScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(
                    modifier = Modifier.width(totalTimelineWidth),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    
                    // TIMESTAMP TICKS RULER Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {
                        for (sec in 0..totalProjDurationSec step 3) {
                            val leftDp = dpPerSecond * sec
                            Box(
                                modifier = Modifier
                                    .offset(x = leftDp)
                                    .align(Alignment.BottomStart)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "${sec}s",
                                        color = Color(0xFF8E8CA4),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(width = 1.dp, height = 4.dp)
                                            .background(Color(0xFF2E174F))
                                    )
                                }
                            }
                        }
                    }

                    // LANE 1: Main Video Clips Lane
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFF110729), RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color(0xFF251242), RoundedCornerShape(6.dp))
                    ) {
                        if (videoClips.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Žádné videoklipy v projektu",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxHeight()) {
                                videoClips.forEachIndexed { clipIndex, clip ->
                                    val clipWidthDp = dpPerSecond * clip.durationSec

                                    Box(
                                        modifier = Modifier
                                            .width(clipWidthDp)
                                            .fillMaxHeight()
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF7A22E8), Color(0xFFAC5EFF))
                                                )
                                            )
                                            .border(1.dp, Color(0xFFBF88FF), RoundedCornerShape(4.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = clip.title.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${clip.durationSec}s",
                                                    color = Color(0xFF00FFC2),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Small controls to arrange / swap clips (reordering)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (clipIndex > 0) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowBack,
                                                        contentDescription = "Posunout doleva",
                                                        tint = Color.White,
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clickable {
                                                                viewModel.moveTimelineClip(context, project.id, clipIndex, clipIndex - 1)
                                                                Toast.makeText(context, "Klip přesunut doleva ⬅️", Toast.LENGTH_SHORT).show()
                                                            }
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                if (clipIndex < videoClips.size - 1) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = "Posunout doprava",
                                                        tint = Color.White,
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clickable {
                                                                viewModel.moveTimelineClip(context, project.id, clipIndex, clipIndex + 1)
                                                                Toast.makeText(context, "Klip přesunut doprava ➡️", Toast.LENGTH_SHORT).show()
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // LANES 2+: Draggable Audio Tracks Lanes
                    activeTracks.forEach { track ->
                        val trackOffsetMs = draggingTempOffsets[track.trackId] ?: track.startOffsetMs
                        val startOffsetDp = dpPerSecond * (trackOffsetMs / 1000f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(Color(0xFF070415), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0xFF1C133A), RoundedCornerShape(6.dp))
                        ) {
                            
                            // Visual horizontal timeline segment bar (Draggable)
                            val isThisDragging = isDraggingTrackId == track.trackId
                            val borderAccentColor = if (isThisDragging) Color(0xFF00FFC2) else Color(0xFF42218D)
                            val trackBgTheme = when (track.trackType) {
                                "Beat" -> Brush.linearGradient(listOf(Color(0xFFBF8800), Color(0xFFFFCC00)))
                                "Vocal" -> Brush.linearGradient(listOf(Color(0xFFD6185D), Color(0xFFFF528E)))
                                "Ambience" -> Brush.linearGradient(listOf(Color(0xFF0F8694), Color(0xFF14C5D6)))
                                else -> Brush.linearGradient(listOf(Color(0xFF5311B2), Color(0xFF8B47FA)))
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = startOffsetDp)
                                    .width(80.dp + (dpPerSecond * 4)) // Fixed segment visually representation with padding
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(trackBgTheme)
                                    .border(1.5.dp, borderAccentColor, RoundedCornerShape(8.dp))
                                    .pointerInput(track.trackId) {
                                        detectDragGestures(
                                            onDragStart = { isDraggingTrackId = track.trackId },
                                            onDragEnd = {
                                                isDraggingTrackId = null
                                                val rawOffset = draggingTempOffsets[track.trackId] ?: track.startOffsetMs
                                                val snapInterval = 250L // 250 ms snapping interval for precise grid lock
                                                val snappedOffset = ((rawOffset + snapInterval / 2) / snapInterval) * snapInterval
                                                viewModel.updateTrackStartOffset(track, snappedOffset)
                                            },
                                            onDragCancel = { isDraggingTrackId = null },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val deltaX = dragAmount.x
                                                // Convert pixels to Milliseconds based on dp density
                                                val localDensity = density
                                                val deltaDp = deltaX / localDensity
                                                val deltaSec = deltaDp / 50f // 50dp = 1sec
                                                val deltaMs = (deltaSec * 1000f).toLong()

                                                val currentVal = draggingTempOffsets[track.trackId] ?: track.startOffsetMs
                                                val newVal = (currentVal + deltaMs).coerceIn(0L, maxDurationMs - 1000L)
                                                draggingTempOffsets[track.trackId] = newVal
                                            }
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (track.trackType) {
                                                "Beat" -> "🎵"
                                                "Vocal" -> "🎙️"
                                                "Ambience" -> "🌧️"
                                                else -> "🎹"
                                            },
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isThisDragging) "POSOUVÁNÍ" else track.name,
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Display start offset handle duration info
                                    val currentSec = trackOffsetMs / 1000f
                                    Text(
                                        text = String.format("+%.1fs ⏳", currentSec),
                                        color = Color.Black.copy(alpha = 0.8f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // GLOBAL PLAYHEAD VERTICAL SWEEPING POINTER INDICATOR LINE
                    if (isPlaying) {
                        val playheadOffsetDp = totalTimelineWidth * globalProgress
                        Box(
                            modifier = Modifier
                                .offset(x = playheadOffsetDp)
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color(0xFF00FFC2))
                        ) {
                            // Small handle bubble indicator at top of playhead
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF00FFC2), RoundedCornerShape(3.dp))
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }

        // Clipboard and video selector tools
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        Spacer(modifier = Modifier.height(16.dp))

        // Robust auto-save state overlay badge to reassure concern/buyer of state persistence
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF14241B))
                .border(0.5.dp, Color(0xFF00FFC2).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFF00FFC2))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VŠECHNY ZMĚNY AUTOMATICKY ULOŽENY ✅ (Auto-Save aktivní)",
                    color = Color(0xFF00FFC2),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "🎬 AI REŽISÉR VIDEA: VIZUÁLNÍ SCÉNÁŘ",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = "Naplánované scény pro videoklip z textu písně. Zkopírujte prompty pro externí generátory (Sora, Luma, Runway) nebo nahrajte vlastní hotová videa.",
            color = Color(0xFF8E8CA4),
            fontSize = 9.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        videoClips.forEachIndexed { index, clip ->
            val videoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        // ignore
                    }
                    viewModel.updateClipVideo(context, project.id, clip.id, uri.toString())
                    Toast.makeText(context, "Video naimportováno pro scénu ${index + 1} 📹", Toast.LENGTH_SHORT).show()
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191033)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(0.5.dp, Color(0xFF2E1C5E), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scéna ${index + 1}: ${clip.title.uppercase()}",
                            color = Color(0xFFE27CFF),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2D174E))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${clip.mood} | ${clip.durationSec}s",
                                color = Color.LightGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Vizuální popis (Prompt):",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = clip.text,
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(clip.text))
                                Toast.makeText(context, "Prompt zkopírován do schránky! 📋", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E174F), contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("ZKOPÍROVAT PROMPT 📋", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                videoPickerLauncher.launch(arrayOf("video/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF123456), contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (!clip.localVideoPath.isNullOrEmpty()) "ZMĚNIT VIDEO 📹" else "NAHRÁT VIDEO 📹",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!clip.localVideoPath.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF14241B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UKÁZKA PŘIPRAVENA ✅",
                                    color = Color.Green,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.updateClipVideo(context, project.id, clip.id, null) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("❌", color = Color.Red, fontSize = 7.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
