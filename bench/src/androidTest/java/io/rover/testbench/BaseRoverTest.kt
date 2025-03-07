package io.rover.testbench

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.karumi.shot.OrchestratorScreenshotSaver
import com.karumi.shot.ScreenshotTest
import io.rover.sdk.experiences.rich.compose.ui.FileExperience
import org.junit.Rule

open class BaseRoverTest : ScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    companion object {
        fun loadTestFile(fileResource: String): Uri {
            return Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(OrchestratorScreenshotSaver.packageName)
                    .appendPath(fileResource)
                    .build()
        }
    }

    fun loadLightModeContent(fileUri: Uri) {
        composeRule.setContent {
            FileExperience(
                    fileUrl = fileUri,
                    modifier = Modifier
                            .width(with(LocalDensity.current) { 1440.toDp() })
                            .height(with(LocalDensity.current) { 2800.toDp() })
            )
        }
    }

    fun loadDarkModeContent(fileUri: Uri) {
        //TODO: Find a good way to force dark mode, since we use isSystemInDarkTheme() to determine dark mode value in appearance
        composeRule.setContent {
            FileExperience(
                    fileUrl = fileUri,
                    modifier = Modifier
                            .width(with(LocalDensity.current) { 1440.toDp() })
                            .height(with(LocalDensity.current) { 2800.toDp() })
            )
        }
    }
}
