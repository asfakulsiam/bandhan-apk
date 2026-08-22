package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BandhanAmber
import com.example.ui.theme.BandhanCyan
import com.example.ui.theme.BandhanDarkNavy
import com.example.ui.theme.BandhanEmeraldPrimary
import com.example.update.UpdateInfo
import com.example.update.UpdateState
import java.io.File
import java.util.Locale

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onStartDownload: (UpdateInfo) -> Unit,
    onCancelDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onOpenPermissionSettings: () -> Unit,
    canInstallPackages: Boolean,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    when (updateState) {
        is UpdateState.Idle -> { /* No dialog */ }

        is UpdateState.Checking -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = BandhanDarkNavy)
                    }
                },
                icon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = BandhanEmeraldPrimary,
                        strokeWidth = 3.dp
                    )
                },
                title = {
                    Text(
                        text = "আপডেট চেক করা হচ্ছে...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = BandhanDarkNavy
                    )
                },
                text = {
                    Text(
                        text = "সর্বশেষ সংস্করণের তথ্য যাচাই করা হচ্ছে। অনুগ্রহ করে অপেক্ষা করুন।",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        is UpdateState.UpToDate -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BandhanEmeraldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Up to Date",
                            tint = BandhanEmeraldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "অ্যাপটি আপ-টু-ডেট আছে",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BandhanDarkNavy,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "আপনি বন্ধন'১৭ অ্যাপের সর্বশেষ সংস্করণ ব্যবহার করছেন।",
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "Current Version: v${updateState.currentVersion}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BandhanDarkNavy,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = BandhanEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("uptodate_ok_button")
                    ) {
                        Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        is UpdateState.UpdateAvailable -> {
            val info = updateState.updateInfo
            UpdateAvailableContent(
                info = info,
                onDismiss = onDismiss,
                onStartDownload = { onStartDownload(info) }
            )
        }

        is UpdateState.Downloading -> {
            val percent = updateState.progressPercent
            val downloadedMB = formatBytes(updateState.downloadedBytes)
            val totalMB = formatBytes(updateState.totalBytes)
            val animatedProgress by animateFloatAsState(
                targetValue = (percent / 100f).coerceIn(0f, 1f),
                label = "download_progress"
            )

            AlertDialog(
                onDismissRequest = { /* prevent outside dismissal during download */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BandhanCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloading",
                            tint = BandhanCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "নতুন সংস্করণ ডাউনলোড হচ্ছে...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BandhanDarkNavy,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${updateState.updateInfo.releaseTitle}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("update_progress_bar"),
                            color = BandhanEmeraldPrimary,
                            trackColor = Color(0xFFE5E7EB)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$percent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BandhanEmeraldPrimary
                            )
                            Text(
                                text = "$downloadedMB / $totalMB",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(
                        onClick = onCancelDownload,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("cancel_download_button")
                    ) {
                        Text("বাতিল করুন", color = Color(0xFFDC2626))
                    }
                }
            )
        }

        is UpdateState.Downloaded -> {
            val file = updateState.apkFile
            val info = updateState.updateInfo

            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BandhanEmeraldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Install Ready",
                            tint = BandhanEmeraldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "ডাউনলোড সম্পন্ন!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BandhanDarkNavy,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${info.releaseTitle} সফলভাবে ডাউনলোড হয়েছে। এখনই ইনস্টল করতে নিচের বাটনে চাপুন।",
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )

                        if (!canInstallPackages) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BandhanAmber.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Permission Notice",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ইনস্টল করার জন্য 'Unknown App' পারমিশন চালু করতে হবে।",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (canInstallPackages) {
                                onInstallApk(file)
                            } else {
                                onOpenPermissionSettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BandhanEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("install_now_button")
                    ) {
                        Text(
                            text = if (canInstallPackages) "ইনস্টল করুন" else "অনুমতি দিন",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("পরে", color = Color.Gray)
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "আপডেট চেক ব্যর্থ হয়েছে",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BandhanDarkNavy,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = updateState.message,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = BandhanEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("retry_update_button")
                    ) {
                        Text("পুনরায় চেষ্টা করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    info: UpdateInfo,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        modifier = Modifier.testTag("update_available_dialog"),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BandhanEmeraldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NewReleases,
                    contentDescription = "Update Available",
                    tint = BandhanEmeraldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "নতুন সংস্করণ উপলব্ধ!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = BandhanDarkNavy,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = info.releaseTitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Version Badges & Size Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = "Current: v${info.currentVersionName}",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text("➔", color = BandhanEmeraldPrimary, fontWeight = FontWeight.Bold)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BandhanEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Latest: ${info.latestVersionName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BandhanEmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (info.apkSizeBytes > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = formatBytes(info.apkSizeBytes),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Release Notes Section
                Text(
                    text = "রিলিজ নোট ও পরিবর্তনসমূহ:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BandhanDarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = cleanMarkdown(info.releaseNotes),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }

                if (info.htmlUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "View release on GitHub",
                                modifier = Modifier.size(16.dp),
                                tint = BandhanCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GitHub এ বিস্তারিত দেখুন",
                                fontSize = 12.sp,
                                color = BandhanCyan
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartDownload,
                colors = ButtonDefaults.buttonColors(containerColor = BandhanEmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("update_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ডাউনলোড করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("remind_later_button")
            ) {
                Text("পরে মনে করিয়ে দিন", color = Color.Gray)
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format(Locale.US, "%.1f MB", mb)
}

private fun cleanMarkdown(markdown: String): String {
    if (markdown.isBlank()) return "উন্নতি এবং বাগ ফিক্স অন্তর্ভুক্ত রয়েছে।"
    return markdown
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("\\*(.*?)\\*"), "$1")
        .replace(Regex("`([^`]+)`"), "$1")
        .trim()
}
