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

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("shot")
    id("com.google.firebase.crashlytics")
}

val roverSdkVersion: String by rootProject.extra
val kotlinVersion: String by rootProject.extra
val composeBomVersion: String by rootProject.extra
val composeKotlinCompilerExtensionVersion: String by rootProject.extra


kotlin {
    jvmToolchain(11)
}

android {
    namespace = "io.rover.testbench"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.rover.testbench"
        minSdk = 26
        targetSdk = 34
        versionCode = 20
        versionName = roverSdkVersion

        testInstrumentationRunner = "com.karumi.shot.ShotTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["auth0Domain"] = "@string/com_auth0_domain"
        manifestPlaceholders["auth0Scheme"] = "demo"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // This allows the Network Security Policy to apply.
            isDebuggable = true
            
            // Apply signing configuration
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            // Apply the same signing configuration to debug builds when available
            if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeKotlinCompilerExtensionVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    shot {
        tolerance =  15.0
    }
    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Coroutines support for Reactive Streams
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.1")

    // Rover SDK
    implementation(project(":core"))
    implementation(project(":notifications"))
    implementation(project(":experiences"))
    implementation(project(":ticketmaster"))
    implementation(project(":location"))
    implementation(project(":seatgeek"))
    implementation(project(":adobeExperience"))
    implementation(project(":axs"))

    implementation(project(":debug"))

    // Firebase:
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // OkHttp to do some requests outside of the SDK itself:
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // auth0:
    implementation("com.auth0.android:auth0:3.4.0")
}
