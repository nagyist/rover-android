package io.rover.testbench


import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.sdk.experiences.rich.compose.ui.FileExperience
import io.rover.testbench.test.R
import io.rover.testbench.utils.displayPage
import org.junit.Test

class OverallTestActionsTests: BaseRoverTest() {
    //Test setup
    @OptIn(ExperimentalTestApi::class)
    fun loadOverallTestFile() {
        val fileUri = loadTestFile("${R.raw.overall_test}")

        loadLightModeContent(fileUri)

        composeRule.waitUntilExactlyOneExists(hasContentDescription("Actions"))

        composeRule.onNodeWithContentDescription("Actions")
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Custom Action"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun performsCustomAction() {
        loadOverallTestFile()

        composeRule.onNodeWithContentDescription("carousel").performClick()

        compareScreenshot(composeRule)
    }

    /*

    //These tests are disabled because snapshot tests don't work with external activities

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun performsOpenUrlAction() {
        loadOverallTestFile()

        composeRule.onNodeWithContentDescription("carousel").displayPage(1)

        composeRule.waitUntilExactlyOneExists(hasText("Open URL Action"))
        composeRule.onNodeWithText("Open URL Action").performClick()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun performPresentUrlAction() {
        loadOverallTestFile()

        composeRule.onNodeWithContentDescription("carousel").displayPage(2)

        composeRule.waitUntilExactlyOneExists(hasText("Present URL Action"))
        composeRule.onNodeWithText("Present URL Action").performClick()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun performCloseAction() {
        loadOverallTestFile()

        composeRule.onNodeWithContentDescription("carousel").displayPage(3)

        composeRule.waitUntilExactlyOneExists(hasText("Close Experience Action"))
        composeRule.onNodeWithText("Close Experience Action").performClick()

        compareScreenshot(composeRule)
    }

    */
}

