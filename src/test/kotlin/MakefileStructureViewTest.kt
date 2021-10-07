import com.intellij.testFramework.PlatformTestUtil

class MakefileStructureViewTest : DockDockTestCase() {
    fun testSimple() {
        val filename = "${getTestName(true)}.mk"
        myFixture.configureByFile("$basePath/$filename")
        myFixture.testStructureView {
            PlatformTestUtil.expandAll(it.tree)
            PlatformTestUtil.assertTreeEqual(it.tree, "-simple.mk\n all\n hello\n world\n")
        }
    }

    override fun getBasePath() = "structure"
}
