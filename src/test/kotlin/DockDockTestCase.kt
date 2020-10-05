import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Base test case for the DockDockBuild tests using fixtures.
 */
abstract class DockDockTestCase : BasePlatformTestCase() {
    final override fun getTestDataPath() = "testData"
}
