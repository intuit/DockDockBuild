
class MakefileFoldingTest : DockDockTestCase() {
    fun testRule() = doTest()
    fun testVariable() = doTest()
    fun testDefine() = doTest()

    fun doTest() = myFixture.testFolding("$testDataPath/$basePath/${getTestName(true)}.mk")

    override fun getBasePath() = "folding"
}
