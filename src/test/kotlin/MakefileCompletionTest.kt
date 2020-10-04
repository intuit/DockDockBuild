
class MakefileCompletionTest : DockDockTestCase() {
  fun testSimple() = doTest("b", "c", "d", "${getTestName(true)}.mk")
  fun testTargets() = doTest("a", "${getTestName(true)}.mk")

  fun doTest(vararg variants: String) = myFixture.testCompletionVariants("$basePath/${getTestName(true)}.mk", *variants)

  override fun getBasePath() = "completion"
}