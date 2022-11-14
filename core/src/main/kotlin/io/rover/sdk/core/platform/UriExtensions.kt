@file:JvmName("UriExtensions")

package io.rover.sdk.core.platform

import android.net.Uri
import java.net.URI

fun URI.asAndroidUri(): Uri = Uri.parse(this.toString())