package com.example.update

import java.io.File

/**
 * Data class representing a GitHub release asset.
 */
data class GitHubReleaseAsset(
    val name: String,
    val size: Long,
    val browserDownloadUrl: String,
    val contentType: String
)

/**
 * Summary of an available or checked update.
 */
data class UpdateInfo(
    val latestVersionName: String,
    val currentVersionName: String,
    val isUpdateAvailable: Boolean,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val publishedAt: String,
    val htmlUrl: String
)

/**
 * UI and worker state for in-app update operations.
 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState
    data class UpToDate(val currentVersion: String, val checkedAt: Long) : UpdateState
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val updateInfo: UpdateInfo
    ) : UpdateState
    data class Downloaded(val apkFile: File, val updateInfo: UpdateInfo) : UpdateState
    data class Error(val message: String, val isNetworkError: Boolean) : UpdateState
}
