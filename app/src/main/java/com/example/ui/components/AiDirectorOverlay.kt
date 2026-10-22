package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDirectorOverlay(
    currentPresetName: String,
    currentEv: Int,
    isLevel: Boolean,
    currentIso: Int,
    currentShutter: Long,
    onGetFastAdvice: suspend (String) -> String,
    onGetDeepThinking: suspend (String) -> String,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var adviceText by remember { mutableStateOf<String?>(null) }
    var deepCritiqueText by remember { mutableStateOf<String?>(null) }
    var isLoadingFast by remember { mutableStateOf(false) }
    var isLoadingDeep by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Floating AI Director Pill
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF2E1A47), Color(0xFF1B233C))
                )
            )
            .border(1.dp, Color(0xFFAB47BC).copy(alpha = 0.6f), CircleShape)
            .clickable {
                showSheet = true
                if (adviceText == null) {
                    isLoadingFast = true
                    scope.launch {
                        adviceText = onGetFastAdvice("General photography composition")
                        isLoadingFast = false
                    }
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Director",
            tint = Color(0xFFE1BEE7),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "AI DIRECTOR",
            color = Color(0xFFE1BEE7),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color(0xFF13151D)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ON-DEVICE OPTICAL INTELLIGENCE",
                        color = Color(0xFFFFD54F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Badge(containerColor = Color(0xFF00E676)) {
                        Text("100% OFFLINE (0 TOKENS)", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Advice Card (On-Device Local Heuristics)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D212E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Fast AI",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Instant Composition & Lighting (On-Device)",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingFast) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFD54F)
                            )
                        } else {
                            Text(
                                text = adviceText ?: "Analyzing current scene framing, horizon, and preset exposure...",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                isLoadingFast = true
                                scope.launch {
                                    adviceText = onGetFastAdvice("Quick framing critique")
                                    isLoadingFast = false
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Refresh Advice", color = Color(0xFFFFD54F), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Thinking Deep Optical Card (gemini-4-argon with thinkingLevel: HIGH)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF211E2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Deep Thinking Pro",
                                tint = Color(0xFFCE93D8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pro Optical Physics & Manual Mastering (On-Device)",
                                color = Color(0xFFCE93D8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingDeep) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = Color(0xFFCE93D8), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Computing optical MTF, SNR, and diffraction limit...", color = Color.LightGray, fontSize = 10.sp)
                            }
                        } else if (deepCritiqueText != null) {
                            Text(
                                text = deepCritiqueText!!,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            Text(
                                text = "Runs on-device physics calculations: Airy disk diffraction limits, sensor photon shot noise, SNR in dB, and shutter/ISO exposure triangulation.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isLoadingDeep = true
                                scope.launch {
                                    deepCritiqueText = onGetDeepThinking("Comprehensive exposure and lens critique")
                                    isLoadingDeep = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Run Optical Physics Analysis (On-Device)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
