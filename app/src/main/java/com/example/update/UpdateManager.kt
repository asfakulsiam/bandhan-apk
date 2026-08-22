package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class UpdateManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    /**
     * Checks the GitHub Releases API for the latest published release.
     */
    fun checkForUpdates(
        owner: String = BuildConfig.GITHUB_REPO_OWNER,
        repo: String = BuildConfig.GITHUB_REPO_NAME,
        currentVersion: String = BuildConfig.VERSION_NAME
    ): Flow<UpdateState> = flow {
        emit(UpdateState.Checking)

        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "Bandhan17-Android-App")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    if (code == 404) {
                        emit(UpdateState.UpToDate(currentVersion, System.currentTimeMillis()))
                    } else {
                        emit(
                            UpdateState.Error(
                                message = "GitHub API response error (HTTP $code)",
                                isNetworkError = false
                            )
                        )
                    }
                    return@flow
                }

                val bodyString = response.body?.string().orEmpty()
                if (bodyString.isBlank()) {
                    emit(UpdateState.Error("Empty response from GitHub Releases API", false))
                    return@flow
                }

                val json = JSONObject(bodyString)
                val tagName = json.optString("tag_name", "").trim()
                val releaseName = json.optString("name", tagName)
                val bodyMarkdown = json.optString("body", "No release notes provided.")
                val publishedAt = json.optString("published_at", "")
                val htmlUrl = json.optString("html_url", "")

                // Parse assets to find APK download asset
                val assetsJson: JSONArray? = json.optJSONArray("assets")
                var apkAsset: GitHubReleaseAsset? = null

                if (assetsJson != null) {
                    for (i in 0 until assetsJson.length()) {
                        val assetObj = assetsJson.optJSONObject(i) ?: continue
                        val assetName = assetObj.optString("name", "")
                        val downloadUrl = assetObj.optString("browser_download_url", "")
                        val size = assetObj.optLong("size", 0L)
                        val contentType = assetObj.optString("content_type", "")

                        if (assetName.endsWith(".apk", ignoreCase = true) ||
                            downloadUrl.endsWith(".apk", ignoreCase = true)
                        ) {
                            apkAsset = GitHubReleaseAsset(
                                name = assetName,
                                size = size,
                                browserDownloadUrl = downloadUrl,
                                contentType = contentType
                            )
                            break
                        }
                    }
                }

                if (apkAsset == null) {
                    // If no APK asset is found on the latest release
                    emit(
                        UpdateState.Error(
                            message = "No APK asset attached to release $tagName",
                            isNetworkError = false
                        )
                    )
                    return@flow
                }

                val isNewer = VersionComparator.isNewerVersion(tagName, currentVersion)
                val updateInfo = UpdateInfo(
                    latestVersionName = tagName,
                    currentVersionName = currentVersion,
                    isUpdateAvailable = isNewer,
                    releaseTitle = if (releaseName.isNotBlank()) releaseName else "Bandhan'17 $tagName",
                    releaseNotes = bodyMarkdown,
                    apkDownloadUrl = apkAsset.browserDownloadUrl,
                    apkFileName = apkAsset.name,
                    apkSizeBytes = apkAsset.size,
                    publishedAt = formatDate(publishedAt),
                    htmlUrl = htmlUrl
                )

                if (isNewer) {
                    emit(UpdateState.UpdateAvailable(updateInfo))
                } else {
                    emit(UpdateState.UpToDate(currentVersion, System.currentTimeMillis()))
                }
            }
        } catch (e: IOException) {
            emit(
                UpdateState.Error(
                    message = "Network error while checking updates: ${e.localizedMessage ?: "Connection timed out"}",
                    isNetworkError = true
                )
            )
        } catch (e: Exception) {
            emit(
                UpdateState.Error(
                    message = "Failed to parse update info: ${e.localizedMessage ?: "Unknown error"}",
                    isNetworkError = false
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Downloads the APK file with progress reporting.
     */
    fun downloadApk(
        context: Context,
        updateInfo: UpdateInfo
    ): Flow<UpdateState> = flow {
        emit(
            UpdateState.Downloading(
                progressPercent = 0,
                downloadedBytes = 0L,
                totalBytes = updateInfo.apkSizeBytes,
                updateInfo = updateInfo
            )
        )

        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val cleanVersion = updateInfo.latestVersionName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val targetFile = File(updatesDir, "Bandhan17_$cleanVersion.apk")

        val request = Request.Builder()
            .url(updateInfo.apkDownloadUrl)
            .header("User-Agent", "Bandhan17-Android-App")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(
                        UpdateState.Error(
                            message = "Failed to download update APK (HTTP ${response.code})",
                            isNetworkError = false
                        )
                    )
                    return@flow
                }

                val body = response.body
                if (body == null) {
                    emit(UpdateState.Error("Empty download stream from GitHub", false))
                    return@flow
                }

                val contentLength = body.contentLength().let {
                    if (it > 0) it else updateInfo.apkSizeBytes
                }

                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        var lastEmittedPercent = -1

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (!currentCoroutineContext().isActive) {
                                targetFile.delete()
                                throw CancellationException("Download cancelled")
                            }

                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            val percent = if (contentLength > 0) {
                                ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
                            } else {
                                0
                            }

                            if (percent != lastEmittedPercent || totalBytesRead == contentLength) {
                                lastEmittedPercent = percent
                                emit(
                                    UpdateState.Downloading(
                                        progressPercent = percent,
                                        downloadedBytes = totalBytesRead,
                                        totalBytes = contentLength,
                                        updateInfo = updateInfo
                                    )
                                )
                            }
                        }
                        outputStream.flush()
                    }
                }

                emit(UpdateState.Downloaded(targetFile, updateInfo))
            }
        } catch (e: CancellationException) {
            targetFile.delete()
            emit(UpdateState.Idle)
        } catch (e: IOException) {
            targetFile.delete()
            emit(
                UpdateState.Error(
                    message = "Download interrupted: ${e.localizedMessage ?: "Network connection lost"}",
                    isNetworkError = true
                )
            )
        } catch (e: Exception) {
            targetFile.delete()
            emit(
                UpdateState.Error(
                    message = "Download error: ${e.localizedMessage ?: "Unknown error"}",
                    isNetworkError = false
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if the app has permission to install unknown apps (Android 8.0+).
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens the Unknown App Install permission settings for this package.
     */
    fun openUnknownAppInstallSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    /**
     * Launches the Android Package Installer for the downloaded APK file.
     */
    fun launchPackageInstaller(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) return false

        return try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun formatDate(isoString: String): String {
        if (isoString.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = parser.parse(isoString)
            if (date != null) formatter.format(date) else isoString
        } catch (e: Exception) {
            isoString.substringBefore("T")
        }
    }
}
