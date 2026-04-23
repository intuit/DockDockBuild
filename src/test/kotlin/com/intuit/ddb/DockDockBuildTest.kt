package com.intuit.ddb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DockDockBuildTest {
    @Test
    fun testGetDefaultDockerPathReturnsExistingFile() {
        val path = getDefaultDockerPath()
        // On any CI/dev machine with docker installed, the returned path should exist
        // or fall back to "docker" (for PATH-based resolution)
        assertTrue(
            "Expected existing file or 'docker' fallback, got: $path",
            path == "docker" || File(path).exists(),
        )
    }

    @Test
    fun testGetDefaultDockerPathNotEmpty() {
        assertFalse(getDefaultDockerPath().isEmpty())
    }

    @Test
    fun testGetMakefileFilenameExtractsName() {
        assertEquals("Makefile", getMakefileFilename("/some/path/to/Makefile"))
        assertEquals("build.mk", getMakefileFilename("/project/build.mk"))
    }

    @Test
    fun testGetDefaultDockerfileDirAppendsDocker() {
        val result = getDefaultDockerfileDir("/project/subdir/Makefile")
        assertEquals("/project/subdir/docker", result)
    }

    @Test
    fun testGetDefaultM2PathContainsDotM2() {
        assertNotEquals("", getDefaultM2Path())
        assertTrue(getDefaultM2Path().endsWith("/.m2"))
    }
}
