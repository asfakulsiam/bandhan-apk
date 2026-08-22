package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BandhanCyan
import com.example.ui.theme.BandhanDarkNavy
import com.example.ui.theme.BandhanEmeraldPrimary
import com.example.ui.theme.BandhanMintBg

@Composable
fun SplashScreen(
    visible: Boolean,
    modifier: Modifier = Modifier,
    animateEmblem: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(600, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        val scaleAnim = remember { Animatable(0.85f) }
        val alphaAnim = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            scaleAnim.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }
        LaunchedEffect(Unit) {
            alphaAnim.animateTo(1f, animationSpec = tween(500, easing = LinearEasing))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF9FCFB),
                            Color(0xFFEBF6F1),
                            Color(0xFFDFEFE8)
                        )
                    )
                )
                .testTag("splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            // Background Leaf / Nature Silhouettes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val leafColor = Color(0xFF0F6B56).copy(alpha = 0.07f)
                val accentCircleColor = Color(0xFF00A3B5).copy(alpha = 0.08f)

                // Top Right Organic Leaf Branch
                val topLeafPath = Path().apply {
                    moveTo(w * 0.70f, 0f)
                    cubicTo(w * 0.85f, h * 0.08f, w * 0.95f, h * 0.15f, w, h * 0.20f)
                    lineTo(w, 0f)
                    close()
                }
                drawPath(topLeafPath, leafColor)

                // Top Right decorative leaf veins & dots
                drawCircle(
                    color = accentCircleColor,
                    radius = 24.dp.toPx(),
                    center = Offset(w * 0.82f, h * 0.12f)
                )
                drawCircle(
                    color = accentCircleColor,
                    radius = 12.dp.toPx(),
                    center = Offset(w * 0.72f, h * 0.18f)
                )

                // Bottom Left Organic Leaf Curve
                val bottomLeafPath = Path().apply {
                    moveTo(0f, h * 0.75f)
                    cubicTo(w * 0.15f, h * 0.82f, w * 0.25f, h * 0.92f, w * 0.30f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(bottomLeafPath, leafColor)

                // Bottom Right soft wave
                val bottomWavePath = Path().apply {
                    moveTo(w * 0.60f, h)
                    cubicTo(w * 0.75f, h * 0.88f, w * 0.90f, h * 0.85f, w, h * 0.90f)
                    lineTo(w, h)
                    close()
                }
                drawPath(bottomWavePath, leafColor)
            }

            // Central Branding Elements
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .padding(32.dp)
            ) {
                // Emblem
                BandhanEmblem(size = 140.dp, animate = animateEmblem)

                Spacer(modifier = Modifier.height(28.dp))

                // Bengali Brand Name: বন্ধন ১৭
                Text(
                    text = stringResource(R.string.app_name_bengali),
                    color = BandhanEmeraldPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bengali Motto: আমরা একসাথে, আমরা এগিয়ে
                Text(
                    text = stringResource(R.string.app_motto_bengali),
                    color = BandhanDarkNavy.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Loading Indicator
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = BandhanEmeraldPrimary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}
