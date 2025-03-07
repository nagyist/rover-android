/*
 * Copyright (c) 2023, Rover Labs, Inc. All rights reserved.
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Rover.
 *
 * This copyright notice shall be included in all copies or substantial portions of
 * the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.testbench.BaseRoverTest
import io.rover.testbench.test.R
import io.rover.testbench.utils.displayPage
import org.junit.Before
import org.junit.Test

class OverallTestShadowTests: BaseRoverTest() {
    //Test setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadOverallTestFile() {
        val fileUri = loadTestFile("${R.raw.overall_test}")

        loadLightModeContent(fileUri)

        //wait for it to load
        composeRule.waitUntilExactlyOneExists(hasContentDescription("Shadows"))

        composeRule.onNodeWithContentDescription("Shadows")
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Text Shadow"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersTextShadow() {
        compareScreenshot(composeRule)
    }

    //Add test for font families
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersMaskShadow() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(1)

        composeRule.waitUntilExactlyOneExists(hasText("Mask Shadow"))
        composeRule.onNodeWithText("Mask Shadow")
                .assertExists("Unable to find Mask Shadow text node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }

    //Add test for text properties
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersImageShadow() {
        composeRule.onNodeWithContentDescription("carousel").displayPage(2)

        composeRule.waitUntilExactlyOneExists(hasText("Image Shadow"))
        composeRule.onNodeWithText("Image Shadow")
                .assertExists("Unable to find Text Properties text node")
                .assertIsDisplayed()

        compareScreenshot(composeRule)
    }
}