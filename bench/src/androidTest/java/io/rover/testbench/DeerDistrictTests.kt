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

package io.rover.testbench

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.rover.sdk.experiences.rich.compose.ui.FileExperience
import org.junit.Test
import io.rover.testbench.test.R
import org.junit.Before

class DeerDistrictTests: BaseRoverTest() {
    //Test Setup
    @Before
    @OptIn(ExperimentalTestApi::class)
    fun loadTestFile() {
        val fileUri = loadTestFile("${R.raw.deer_district}")

        loadLightModeContent(fileUri)

        //wait for it to load
        composeRule.waitUntilExactlyOneExists(hasContentDescription("content"))
    }

    @Test
    fun rendersInitialView() {
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersFeaturedSection() {
        composeRule.onNodeWithContentDescription("Featured")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersEventsSection() {
        composeRule.onNodeWithContentDescription("Events")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersNewsSection() {
        composeRule.onNodeWithContentDescription("News")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @Test
    fun rendersExploreSection() {
        composeRule.onNodeWithContentDescription("Explore")
                .assertExists()
                .performScrollTo()
                .assertIsDisplayed()

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersEventsScreen() {
        composeRule.onNodeWithContentDescription("All Events Button")
                .assertExists()
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasContentDescription("Events Content"))

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersNewsScreen() {
        composeRule.onNodeWithContentDescription("Latest News Button")
                .assertExists()
                .performScrollTo()
                .performClick()

        composeRule.waitUntilExactlyOneExists(hasContentDescription("News Content"))

        Thread.sleep(500)
        compareScreenshot(composeRule)
    }
}