import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/** The private release key is local and must never be checked into Git. */
val releaseKeyFile = rootProject.file("build-key")
val releasePropertiesFile = rootProject.file("keystore.properties")
val releaseProperties = Properties().apply {
    if (releasePropertiesFile.isFile) {
        releasePropertiesFile.inputStream().use(::load)
    }
}
val releaseSigningReady = releaseKeyFile.isFile &&
    listOf("storePassword", "keyPassword", "keyAlias").all {
        !releaseProperties.getProperty(it).isNullOrBlank()
    }

val verifyReleaseSigning by tasks.registering {
    group = "verification"
    description = "Require the private build-key and keystore.properties for signed release outputs"
    doLast {
        check(releaseSigningReady) {
            "Release signing is not configured. Put build-key and keystore.properties " +
                "at the repository root; see docs/RELEASE_SIGNING.md. " +
                "Debug builds do not require a release key."
        }
    }
}

// Never allow unsigned release packaging when credentials are missing.
tasks.matching { it.name in setOf("assembleRelease", "packageRelease", "bundleRelease") }
    .configureEach { dependsOn(verifyReleaseSigning) }

android {
    namespace = "com.kmab.gulp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kmab.gulp"
        minSdk = 28
        targetSdk = 36
        versionCode = 15
        versionName = "0.7.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningReady) {
            create("release") {
                storeFile = releaseKeyFile
                storePassword = releaseProperties.getProperty("storePassword")
                keyAlias = releaseProperties.getProperty("keyAlias")
                keyPassword = releaseProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (releaseSigningReady) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    // ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -Dorg.gradle.java.home="D:/Code/java/jdk-21.0.12.1"
    // ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -Dorg.gradle.java.home="D:/Code/java/jdk-17.0.12"
    // ./gradlew :app:testDebugUnitTest
    // ./gradlew :app:assembleRelease

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.glance.appwidget)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
