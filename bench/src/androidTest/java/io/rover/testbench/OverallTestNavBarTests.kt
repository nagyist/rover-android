package io.rover.testbench

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.sdk.experiences.rich.compose.ui.FileExperience
import io.rover.testbench.test.R
import org.junit.Before

class OverallTestNavBarTests: BaseRoverTest() {
    //Test setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadOverallTestFile() {
        val fileUri = loadTestFile("${R.raw.overall_test}")

        loadLightModeContent(fileUri)

        //wait for it to load
        composeRule.waitUntilExactlyOneExists(hasContentDescription("Text"))

        composeRule.onNodeWithContentDescription("Text")
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Embedded Font"))
    }
}