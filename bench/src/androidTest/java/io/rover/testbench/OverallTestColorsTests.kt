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
import org.junit.Before
import org.junit.Test


class OverallTestColorsTests: BaseRoverTest() {
    //Test setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadOverallTestFile() {
        val fileUri = loadTestFile("${R.raw.overall_test}")

        loadLightModeContent(fileUri)

        composeRule.waitUntilExactlyOneExists(hasContentDescription("Colors"))

        composeRule.onNodeWithContentDescription("Colors")
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Text Fill Color"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersTextColor() {
        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersCustomFillColor() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(1)

        composeRule.waitUntilExactlyOneExists(hasText("Custom Fill Color"))
        composeRule.onNodeWithText("Custom Fill Color")
                .assertExists()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersGradientFillColor() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(2)

        composeRule.waitUntilExactlyOneExists(hasText("Gradient Fill Color"))
        composeRule.onNodeWithText("Gradient Fill Color")
                .assertExists()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersDocumentFillColor() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(3)

        composeRule.waitUntilExactlyOneExists(hasText("Document Fill Color"))
        composeRule.onNodeWithText("Document Fill Color")
                .assertExists()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersClearSystemFillColor() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(4)

        composeRule.waitUntilExactlyOneExists(hasText("Clear System Fill Color"))
        composeRule.onNodeWithText("Clear System Fill Color")
                .assertExists()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersSystemFillColor() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(5)

        composeRule.waitUntilExactlyOneExists(hasText("System Fill Color"))
        composeRule.onNodeWithText("System Fill Color")
                .assertExists()

        compareScreenshot(composeRule)
    }
}

