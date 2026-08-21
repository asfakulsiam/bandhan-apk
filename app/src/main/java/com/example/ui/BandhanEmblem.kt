package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BandhanCyan
import com.example.ui.theme.BandhanDarkNavy
import com.example.ui.theme.BandhanEmeraldPrimary

@Composable
fun BandhanEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    animate: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emblem_anim")
    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static"
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(12.dp, CircleShape, spotColor = BandhanEmeraldPrimary.copy(alpha = 0.25f))
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // Outer Cyan Ring
            drawCircle(
                color = Color(0xFF00A3B5),
                radius = radius - 6.dp.toPx(),
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner Dark Teal Ring
            drawCircle(
                color = Color(0xFF0E4957),
                radius = radius - 14.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center decorative inner ring
            drawCircle(
                color = Color(0xFF00A3B5).copy(alpha = 0.4f),
                radius = radius - 30.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Center Interlocked Hands Graphic
        Canvas(
            modifier = Modifier
                .size(size * 0.52f)
                .rotate(rotation)
        ) {
            val w = this.size.width
            val h = this.size.height
            val skinColor = Color(0xFFFDBA74)
            val strokeColor = Color(0xFF1E293B)
            val strokeWidth = 2.dp.toPx()

            // 4 Sleeves: Top (Blue), Right (Slate), Bottom (Teal), Left (Red)
            // Top Sleeve (Blue)
            drawRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(w * 0.35f, 0f),
                size = Size(w * 0.30f, h * 0.22f)
            )

            // Right Sleeve (Dark Slate)
            drawRect(
                color = Color(0xFF334155),
                topLeft = Offset(w * 0.78f, h * 0.35f),
                size = Size(w * 0.22f, h * 0.30f)
            )

            // Bottom Sleeve (Teal Green)
            drawRect(
                color = Color(0xFF059669),
                topLeft = Offset(w * 0.35f, h * 0.78f),
                size = Size(w * 0.30f, h * 0.22f)
            )

            // Left Sleeve (Coral Red)
            drawRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(0f, h * 0.35f),
                size = Size(w * 0.22f, h * 0.30f)
            )

            // Central Interlocked Hands Path
            val armPath1 = Path().apply {
                moveTo(w * 0.28f, h * 0.28f)
                lineTo(w * 0.65f, h * 0.28f)
                lineTo(w * 0.65f, h * 0.45f)
                lineTo(w * 0.52f, h * 0.45f)
                lineTo(w * 0.40f, h * 0.40f)
                close()
            }
            drawPath(armPath1, skinColor)
            drawPath(armPath1, strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))

            val armPath2 = Path().apply {
                moveTo(w * 0.72f, h * 0.28f)
                lineTo(w * 0.72f, h * 0.65f)
                lineTo(w * 0.55f, h * 0.65f)
                lineTo(w * 0.55f, h * 0.52f)
                lineTo(w * 0.60f, h * 0.40f)
                close()
            }
            drawPath(armPath2, skinColor)
            drawPath(armPath2, strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))

            val armPath3 = Path().apply {
                moveTo(w * 0.72f, h * 0.72f)
                lineTo(w * 0.35f, h * 0.72f)
                lineTo(w * 0.35f, h * 0.55f)
                lineTo(w * 0.48f, h * 0.55f)
                lineTo(w * 0.60f, h * 0.60f)
                close()
            }
            drawPath(armPath3, skinColor)
            drawPath(armPath3, strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))

            val armPath4 = Path().apply {
                moveTo(w * 0.28f, h * 0.72f)
                lineTo(w * 0.28f, h * 0.35f)
                lineTo(w * 0.45f, h * 0.35f)
                lineTo(w * 0.45f, h * 0.48f)
                lineTo(w * 0.40f, h * 0.60f)
                close()
            }
            drawPath(armPath4, skinColor)
            drawPath(armPath4, strokeColor, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
    }
}
