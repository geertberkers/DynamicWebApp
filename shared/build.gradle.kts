plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.dynamicwebapp.shared"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.compose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.multiplatform.settings)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

// Task for Xcode integration
tasks.register("packForXcode") {
    val xcodeConfiguration = project.findProperty("XCODE_CONFIGURATION")?.toString() ?: "Debug"
    val frameworkName = "shared"
    val frameworkPath = buildDir.resolve("xcode-frameworks").resolve(xcodeConfiguration)
    
    group = "build"
    description = "Pack shared framework for Xcode"
    
    val mode = if (xcodeConfiguration == "Release") "RELEASE" else "DEBUG"
    
    val iosArm64 = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosArm64")
    val iosX64 = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosX64")
    val iosSimulatorArm64 = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosSimulatorArm64")
    
    val iosArm64Framework = iosArm64.binaries.getFramework(mode)
    val iosX64Framework = iosX64.binaries.getFramework(mode)
    val iosSimulatorArm64Framework = iosSimulatorArm64.binaries.getFramework(mode)
    
    dependsOn(iosArm64Framework.linkTask)
    dependsOn(iosX64Framework.linkTask)
    dependsOn(iosSimulatorArm64Framework.linkTask)
    
    doLast {
        val frameworkDir = frameworkPath.resolve("${frameworkName}.xcframework")
        frameworkDir.parentFile.mkdirs()
        frameworkDir.deleteRecursively()
        
        val arm64Path = iosArm64Framework.outputDirectory.resolve("${frameworkName}.framework")
        val x64Path = iosX64Framework.outputDirectory.resolve("${frameworkName}.framework")
        val simulatorArm64Path = iosSimulatorArm64Framework.outputDirectory.resolve("${frameworkName}.framework")
        
        exec {
            commandLine(
                "xcodebuild", "-create-xcframework",
                "-framework", arm64Path.absolutePath,
                "-framework", x64Path.absolutePath,
                "-framework", simulatorArm64Path.absolutePath,
                "-output", frameworkDir.absolutePath
            )
        }
    }
}

