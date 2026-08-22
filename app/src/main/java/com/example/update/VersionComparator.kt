package com.example.update

/**
 * Utility to compare semantic and tag version strings safely.
 */
object VersionComparator {

    /**
     * Checks if [remote] version is strictly newer than [current] version.
     * Examples:
     * - "v1.0.1" is newer than "1.0.0" -> true
     * - "1.1" is newer than "1.0.9" -> true
     * - "v1.0.0" is newer than "1.0.0" -> false
     * - "1.0.0" is newer than "1.0.1" -> false
     */
    fun isNewerVersion(remote: String?, current: String?): Boolean {
        if (remote.isNullOrBlank() || current.isNullOrBlank()) return false

        val remoteClean = cleanVersion(remote)
        val currentClean = cleanVersion(current)

        if (remoteClean == currentClean) return false

        val remoteParts = parseParts(remoteClean)
        val currentParts = parseParts(currentClean)

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val rPart = remoteParts.getOrElse(i) { 0 }
            val cPart = currentParts.getOrElse(i) { 0 }
            if (rPart > cPart) return true
            if (rPart < cPart) return false
        }

        return false
    }

    private fun cleanVersion(ver: String): String {
        return ver.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-") // Handle tags like 1.0.1-rc1 -> 1.0.1
            .trim()
    }

    private fun parseParts(cleanVer: String): List<Int> {
        return cleanVer.split(".")
            .mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }
    }
}
