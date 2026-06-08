package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPastel
import com.example.ui.theme.LemonSoft
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressVisualizerSelector(
    visualizerType: Int, // 0: Circular, 1: Plant Growth, 2: Rocket, 3: Unlocking
    progress: Float,     // Value between 0.0f and 1.0f
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (visualizerType == 2) {
                        // Dark space colors for rocket
                        listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
                    } else {
                        // Warm cream and soft pastel colors
                        listOf(Color(0xFFFFFDF9), Color(0xFFFAF1E4))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (visualizerType) {
            0 -> CircularProgressVisualizer(progress = progress, category = category)
            1 -> PlantGrowthVisualizer(progress = progress)
            2 -> RocketSpaceVisualizer(progress = progress)
            3 -> UnlockingItemVisualizer(progress = progress, category = category)
            else -> CircularProgressVisualizer(progress = progress, category = category)
        }
    }
}

@Composable
fun CircularProgressVisualizer(
    progress: Float,
    category: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(170.dp)
    ) {
        // Background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = GreenPastel.copy(alpha = 0.15f * animatedProgress),
                radius = size.minDimension / 2 * pulseScale
            )
        }

        // Circular progress arc
        Canvas(modifier = Modifier.size(140.dp)) {
            val strokeWidth = 14.dp.toPx()
            // Track
            drawCircle(
                color = Color(0xFFECE5D9),
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            drawArc(
                color = GreenDark,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }

        // Percentage and Category Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getCategoryEmoji(category),
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
            Text(
                text = "Terkumpul",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PlantGrowthVisualizer(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "plantGrowth"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw the floor/dirt line
        drawLine(
            color = Color(0xFFD4C5B0),
            start = Offset(width * 0.1f, height * 0.85f),
            end = Offset(width * 0.9f, height * 0.85f),
            strokeWidth = 4f
        )

        // 2. Draw the Flower Pot
        val potPath = Path().apply {
            moveTo(width * 0.42f, height * 0.85f) // Top left inside pot
            lineTo(width * 0.58f, height * 0.85f) // Top right inside pot
            lineTo(width * 0.56f, height * 0.95f) // Bottom right pot
            lineTo(width * 0.44f, height * 0.95f) // Bottom left pot
            close()
        }
        drawPath(path = potPath, color = Color(0xFFC68B59)) // Terracotta color
        // Pot Lip
        drawRoundRect(
            color = Color(0xFFB17543),
            topLeft = Offset(width * 0.40f, height * 0.83f),
            size = Size(width * 0.20f, height * 0.03f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 3. Draw Stem (Height proportional to progress)
        val maxStemHeight = height * 0.55f // The stem can grow up to 55% of canvas height
        val actualStemHeight = maxStemHeight * animatedProgress
        val stemTopY = height * 0.83f - actualStemHeight

        if (animatedProgress > 0.05f) {
            drawLine(
                color = GreenDark,
                start = Offset(width * 0.5f, height * 0.83f),
                end = Offset(width * 0.5f, stemTopY),
                strokeWidth = 10f
            )
        }

        // 4. Draw Leaves at different milestones
        val leafWidth = 24f
        val leafHeight = 12f

        // Left leaf 1 (appears at 25% progress)
        if (animatedProgress >= 0.25f) {
            val leafY = height * 0.83f - maxStemHeight * 0.25f
            val leafPath = Path().apply {
                moveTo(width * 0.5f, leafY)
                quadraticTo(width * 0.4f, leafY - leafHeight, width * 0.38f, leafY)
                quadraticTo(width * 0.4f, leafY + leafHeight, width * 0.5f, leafY)
            }
            drawPath(path = leafPath, color = GreenPastel)
        }

        // Right leaf 2 (appears at 50% progress)
        if (animatedProgress >= 0.50f) {
            val leafY = height * 0.83f - maxStemHeight * 0.50f
            val leafPath = Path().apply {
                moveTo(width * 0.5f, leafY)
                quadraticTo(width * 0.6f, leafY - leafHeight, width * 0.62f, leafY)
                quadraticTo(width * 0.6f, leafY + leafHeight, width * 0.5f, leafY)
            }
            drawPath(path = leafPath, color = GreenPastel)
        }

        // Left leaf 3 (appears at 75% progress)
        if (animatedProgress >= 0.75f) {
            val leafY = height * 0.83f - maxStemHeight * 0.75f
            val leafPath = Path().apply {
                moveTo(width * 0.5f, leafY)
                quadraticTo(width * 0.42f, leafY - leafHeight, width * 0.4f, leafY)
                quadraticTo(width * 0.42f, leafY + leafHeight, width * 0.5f, leafY)
            }
            drawPath(path = leafPath, color = GreenPastel.copy(alpha = 0.9f))
        }

        // 5. Flower Bloom at exactly 100% (or very near 95%)
        if (animatedProgress >= 0.95f) {
            // Draw beautiful flower petals
            val petalRadius = 14f
            val flowerCenterY = stemTopY

            // 5 Petals
            val petalColor = LemonSoft
            val petalBorder = Color(0xFFFFD166)

            for (i in 0 until 5) {
                val angle = i * (2 * PI / 5)
                val petalX = width * 0.5f + (petalRadius * 1.2 * cos(angle)).toFloat()
                val petalY = flowerCenterY + (petalRadius * 1.2 * sin(angle)).toFloat()
                drawCircle(color = petalColor, radius = petalRadius, center = Offset(petalX, petalY))
                drawCircle(color = petalBorder, radius = petalRadius, center = Offset(petalX, petalY), style = Stroke(width = 2f))
            }

            // Central core
            drawCircle(
                color = Color(0xFFE76F51), // Pretty coral center
                radius = 11f,
                center = Offset(width * 0.5f, flowerCenterY)
            )
        }
    }
}

@Composable
fun RocketSpaceVisualizer(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rocketProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Starry Sky
        val randomStars = listOf(
            Offset(width * 0.15f, height * 0.2f),
            Offset(width * 0.85f, height * 0.15f),
            Offset(width * 0.35f, height * 0.45f),
            Offset(width * 0.75f, height * 0.5f),
            Offset(width * 0.22f, height * 0.75f),
            Offset(width * 0.8f, height * 0.8f),
            Offset(width * 0.55f, height * 0.25f),
            Offset(width * 0.08f, height * 0.55f)
        )

        randomStars.forEachIndexed { idx, starOffset ->
            drawCircle(
                color = Color.White.copy(alpha = if (idx % 2 == 0) alphaAnim else 1f - alphaAnim),
                radius = if (idx % 3 == 0) 4f else 2f,
                center = starOffset
            )
        }

        // 2. Draw Target Planet (Top Center)
        drawCircle(
            color = Color(0xFFE76F51),
            radius = 24f,
            center = Offset(width * 0.5f, height * 0.16f)
        )
        // Planet ring
        val ringPath = Path().apply {
            moveTo(width * 0.5f - 40f, height * 0.16f + 10f)
            quadraticTo(
                width * 0.5f, height * 0.16f + 30f,
                width * 0.5f + 40f, height * 0.16f - 10f
            )
        }
        drawPath(
            path = ringPath,
            color = LemonSoft.copy(alpha = 0.7f),
            style = Stroke(width = 6f)
        )

        // Shining Star for Victory
        if (animatedProgress >= 1.0f) {
            drawCircle(
                color = Color.Yellow,
                radius = 12f,
                center = Offset(width * 0.5f, height * 0.16f)
            )
        }

        // 3. Draw flight path from ground (height*0.85f) to planet (height*0.25f)
        val pathStartY = height * 0.82f
        val pathEndY = height * 0.22f
        val currentRocketY = pathStartY - (pathStartY - pathEndY) * animatedProgress

        // Trait dotted line
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(width * 0.5f, pathStartY),
            end = Offset(width * 0.5f, pathEndY),
            strokeWidth = 3f
        )

        // 4. Draw fire plume under the rocket
        if (animatedProgress > 0.02f) {
            val plumeThick = 12f + 6f * sin(alphaAnim * PI).toFloat()
            val firePath = Path().apply {
                moveTo(width * 0.5f - 6f, currentRocketY + 12f)
                lineTo(width * 0.5f + 6f, currentRocketY + 12f)
                lineTo(width * 0.5f, currentRocketY + 12f + plumeThick)
                close()
            }
            drawPath(
                path = firePath,
                color = Color(0xFFF4A261)
            )
        }

        // 5. Draw Rocket Ship Shape (centered at currentRocketY)
        val rX = width * 0.5f
        val rY = currentRocketY

        // Left Fin
        val finLeft = Path().apply {
            moveTo(rX - 4f, rY + 4f)
            lineTo(rX - 14f, rY + 12f)
            lineTo(rX - 4f, rY + 12f)
            close()
        }
        drawPath(path = finLeft, color = Color(0xFFE76F51))

        // Right Fin
        val finRight = Path().apply {
            moveTo(rX + 4f, rY + 4f)
            lineTo(rX + 14f, rY + 12f)
            lineTo(rX + 4f, rY + 12f)
            close()
        }
        drawPath(path = finRight, color = Color(0xFFE76F51))

        // Rocket Body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(rX - 7f, rY - 14f),
            size = Size(14f, 26f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Nose Cone (Red tip)
        val nosePath = Path().apply {
            moveTo(rX - 7f, rY - 10f)
            quadraticTo(rX, rY - 24f, rX + 7f, rY - 10f)
            close()
        }
        drawPath(path = nosePath, color = Color(0xFFE76F51))

        // Small round window
        drawCircle(
            color = Color(0xFF457B9D),
            radius = 3.5f,
            center = Offset(rX, rY - 3f)
        )
    }
}

@Composable
fun UnlockingItemVisualizer(
    progress: Float,
    category: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "unlockProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF83A990).copy(alpha = 0.2f))
            ) {
                Text(
                    text = getCategoryEmoji(category),
                    fontSize = 44.sp,
                    modifier = Modifier.alpha(0.25f + 0.75f * animatedProgress)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Lock State Notification
            if (animatedProgress < 1.0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEA79).copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔒 Terkunci (${(animatedProgress * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF806A05)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPastel)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔓 Kado Terbuka! 🎉",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark
                    )
                }
            }
        }
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "smartphone", "hp" -> "📱"
        "laptop", "komputer" -> "💻"
        "motor", "kendaraan" -> "🏍️"
        "kamera", "sony" -> "📷"
        "liburan", "bali", "wisata" -> "✈️"
        "pendidikan", "sekolah" -> "🎓"
        "hobi", "mainan" -> "🎮"
        "konser", "tiket" -> "🎟️"
        "perkakas", "kerja" -> "💼"
        "furniture", "kamar" -> "🛏️"
        else -> "🎁"
    }
}
