package io.rover.testbench

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.testbench.test.R
import io.rover.testbench.utils.displayPage
import org.junit.Before
import org.junit.Test

class OverallTestTextTests: BaseRoverTest() {
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersEmbeddedFonts() {
        compareScreenshot(composeRule)
    }

    //Add test for font families
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersFontFamilies() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(1)

        composeRule.waitUntilExactlyOneExists(hasText("Font Family"))
        composeRule.onNodeWithText("Font Family")
                .assertExists("Unable to find Font Family text node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }

    //Add test for text properties
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersTextProperties() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(2)

        composeRule.waitUntilExactlyOneExists(hasText("Text Properties"))
        composeRule.onNodeWithText("Text Properties")
                .assertExists("Unable to find Text Properties text node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }
}

