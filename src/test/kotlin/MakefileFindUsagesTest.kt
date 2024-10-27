import com.intellij.codeInsight.TargetElementUtil
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl

class MakefileFindUsagesTest : DockDockTestCase() {
    fun testSimple() {
        val usages = myFixture.testFindUsages("$basePath/${getTestName(true)}.mk")

        assertEquals(usages.size, 2)
    }

    fun testPhony() = notSearchableForUsages()

    fun testForce() = notSearchableForUsages()

    fun notSearchableForUsages() {
        myFixture.configureByFiles("$basePath/${getTestName(true)}.mk")
        val targetElement =
            TargetElementUtil.findTargetElement(
                myFixture.editor,
                TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED,
            )
        val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)

        assertNull(handler)
    }

    override fun getBasePath() = "findUsages"
}
