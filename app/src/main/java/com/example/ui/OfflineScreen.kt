package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BandhanCyan
import com.example.ui.theme.BandhanDarkNavy
import com.example.ui.theme.BandhanEmeraldPrimary
import com.example.ui.theme.BandhanMintBg
import kotlinx.coroutines.delay

@Composable
fun OfflineScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember { mutableIntStateOf(15) }

    LaunchedEffect(Unit) {
        while (true) {
            secondsLeft = 15
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            // Auto-retry when timer expires
            onRetry()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BandhanMintBg)
            .padding(24.dp)
            .testTag("offline_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Offline Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet Connection",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bengali Title
                Text(
                    text = stringResource(R.string.no_internet_title),
                    color = BandhanDarkNavy,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bengali Description
                Text(
                    text = stringResource(R.string.no_internet_desc),
                    color = BandhanDarkNavy.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Countdown Timer Progress Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { secondsLeft / 15f },
                        modifier = Modifier.size(64.dp),
                        color = BandhanCyan,
                        trackColor = Color(0xFFE2E8F0),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "${secondsLeft}s",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BandhanDarkNavy
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Retry Button
                Button(
                    onClick = {
                        secondsLeft = 15
                        onRetry()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("retry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BandhanEmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.retry_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
