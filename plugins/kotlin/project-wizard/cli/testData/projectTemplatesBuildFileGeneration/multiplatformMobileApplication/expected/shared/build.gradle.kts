import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-android-extensions")
}

group = "me.user"
version = "1.0-SNAPSHOT"

kotlin {
    android()
    ios {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.material:material:1.2.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}

tasks.getByName("build").dependsOn(packForXcode)
