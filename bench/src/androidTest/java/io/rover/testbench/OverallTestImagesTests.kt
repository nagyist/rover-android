package io.rover.testbench

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.sdk.experiences.rich.compose.ui.FileExperience
import org.junit.Test
import io.rover.testbench.test.R
import io.rover.testbench.utils.displayPage
import org.junit.Before

class OverallTestImagesTests: BaseRoverTest() {
    //Test setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadOverallTestFile() {
        val fileUri = loadTestFile("${R.raw.overall_test}")

        loadLightModeContent(fileUri)

        //wait for it to load
        composeRule.waitUntilExactlyOneExists(hasText("Images"))

        composeRule.onNodeWithText("Images")
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Masked Image"))
    }


    @Test
    fun rendersImage() {
        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersRemoteImage() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(4)
        composeRule.waitUntilExactlyOneExists(hasText("Remote image"))
        composeRule.onNodeWithText("Remote image")
                .assertExists("Unable to find remote image description node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersLightModeImage() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(5)
        composeRule.waitUntilExactlyOneExists(hasText("Light vs Dark"))
        composeRule.onNodeWithText("Light vs Dark")
                .assertExists("Unable to find remote image description node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }

    /* Testing animated GIFs is not consistent across devices

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersAnimatedGif() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(3)
        composeRule.waitUntilExactlyOneExists(hasText("This is an animated GIF"))
        composeRule.onNodeWithText("This is an animated GIF")
                .assertExists("Unable to find animated GIF description node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }
    
     */


    //Test for dark mode image
    //TODO: Find a way to set dark mode before the test is run


}