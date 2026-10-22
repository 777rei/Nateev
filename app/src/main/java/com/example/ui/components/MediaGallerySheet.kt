package com.example.ui.components

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.utils.ExifDetails
import com.example.utils.ExifMetadataWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGallerySheet(
    imageUri: String,
    colorMatrix: ColorMatrix,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var exif by remember { mutableStateOf(ExifDetails()) }
    val isVideo = imageUri.contains(".mp4", ignoreCase = true) || imageUri.contains("video", ignoreCase = true)

    LaunchedEffect(imageUri) {
        if (!isVideo) {
            try {
                val uri = Uri.parse(imageUri)
                exif = ExifMetadataWriter.readExif(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF101216)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isVideo) "PROFESSIONAL VIDEO PREVIEW (LUT ACTIVE)" else "MEDIA INSPECTOR & EXIF",
                color = Color(0xFFFFD54F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Media Preview Player / Image Container with active LUT color grading applied
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(imageUri))
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                                })
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Captured Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isVideo) {
                // EXIF Parameter Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExifBadge(label = "FOCAL", value = exif.focalLength)
                    ExifBadge(label = "APERTURE", value = exif.fNumber)
                    ExifBadge(label = "SHUTTER", value = exif.exposureTime)
                    ExifBadge(label = "ISO", value = exif.iso)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Engine details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Profile: ${exif.software}",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (exif.latitude != null && exif.longitude != null) {
                        Text(
                            text = "GPS: ${String.format("%.4f", exif.latitude)}°, ${String.format("%.4f", exif.longitude)}°",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nateev Pro Cinema Video Recording (LUT Applied)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saved in Movies/NateevCamera • Tap video to play/pause",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExifBadge(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
