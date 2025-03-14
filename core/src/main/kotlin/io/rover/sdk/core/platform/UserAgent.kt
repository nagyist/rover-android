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

package io.rover.sdk.core.platform

import android.content.pm.PackageInfo
import io.rover.core.BuildConfig
import okhttp3.Request

internal fun Request.Builder.setRoverUserAgent(packageInfo: PackageInfo) {
    // get the version number of the app we're embedded into (thus can't use BuildConfig for that)

    this.addHeader("User-Agent", packageInfo.roverUserAgent)
}

public val PackageInfo.roverUserAgent: String get() {
    val appDescriptor = "${packageName}/${versionName}"
    val roverDescriptor = "RoverSDK/${BuildConfig.ROVER_SDK_VERSION}"
    return "${System.getProperty("http.agent")} $appDescriptor $roverDescriptor"
}
