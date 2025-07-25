// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("androidx.navigation.safeargs") version "2.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    kotlin("jvm") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.kotlin.compose) apply false
}