import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing is read from keystore.properties (gitignored). Without it the
// release build falls back to the debug key so the build never breaks, but such
// a build cannot update an installed copy in place.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val releaseKeystore = keystoreProperties.getProperty("storeFile")?.let { rootProject.file(it) }

android {
    namespace = "com.thesabihjhatial.cockpit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thesabihjhatial.cockpit"
        minSdk = 26
        targetSdk = 35
        resourceConfigurations += "en"
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (releaseKeystore?.exists() == true) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
    }

    // Nothing here uses reflection over Kotlin metadata or debugging probes.
    packaging {
        resources {
            excludes += listOf(
                "kotlin/**",
                "META-INF/**.version",
                "META-INF/*.kotlin_module",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
            )
        }
        // Compose ships this native path library for animated vector work, which the
        // launcher never does; dropping it saves ~37 KB across four ABIs.
        jniLibs {
            excludes += "**/libandroidx.graphics.path.so"
        }
    }

    // The dependency list AGP embeds in the APK signing block is of no use here.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.palette)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
