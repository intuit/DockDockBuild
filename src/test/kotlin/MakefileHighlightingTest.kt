
class MakefileHighlightingTest : DockDockTestCase() {
    fun testUnresolved() = doTest()
    fun testRedundant() = doTest(true)
    fun testTargetspecificvars() = doTest()

    fun doTest(checkInfos: Boolean = false) {
        myFixture.testHighlighting(
            true, checkInfos, true,
            "$basePath/${getTestName(true)}.mk"
        )
    }

    override fun getBasePath() = "highlighting"
}
