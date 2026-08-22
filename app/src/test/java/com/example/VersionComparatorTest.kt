package com.example

import com.example.update.VersionComparator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun testSemanticVersionComparison() {
        // Newer remote versions
        assertTrue(VersionComparator.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(VersionComparator.isNewerVersion("v1.1.0", "1.0.9"))
        assertTrue(VersionComparator.isNewerVersion("2.0.0", "1.9.9"))
        assertTrue(VersionComparator.isNewerVersion("v1.0.0.1", "1.0.0"))
        assertTrue(VersionComparator.isNewerVersion("v1.0.1-release", "1.0.0"))

        // Same or older versions
        assertFalse(VersionComparator.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("1.0.0", "v1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("v1.0.0", "1.0.1"))
        assertFalse(VersionComparator.isNewerVersion("0.9.9", "1.0.0"))

        // Invalid or null checks
        assertFalse(VersionComparator.isNewerVersion(null, "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("1.0.0", null))
        assertFalse(VersionComparator.isNewerVersion("", "1.0.0"))
    }
}
