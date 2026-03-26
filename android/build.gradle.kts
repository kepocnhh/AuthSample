import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.compose") version Version.compose
    id("org.jetbrains.kotlin.plugin.compose") version Version.kotlin
}

android {
    namespace = "test.android.auth"
    compileSdk = Version.Android.compileSdk

    sourceSets.getByName("main") {
        kotlin.srcDirs("../shared/src/$name/kotlin")
    }

    defaultConfig {
        applicationId = namespace
        minSdk = Version.Android.minSdk
        targetSdk = Version.Android.targetSdk
        versionCode = 1
        versionName = "0.0.1"
    }

    buildTypes {
        getByName("debug") {
            sourceSets.getByName(name) {
                kotlin.srcDirs("../shared/src/$name/kotlin")
            }
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    productFlavors {
        "specifics".also { dimension ->
            flavorDimensions += dimension
            create("real") {
                this.dimension = dimension
                sourceSets.getByName(name) {
                    kotlin.srcDirs("../shared/src/$name/kotlin")
                }
            }
            create("mock") {
                this.dimension = dimension
                sourceSets.getByName(name) {
                    kotlin.srcDirs("../shared/src/$name/kotlin")
                }
                this.applicationIdSuffix = ".$name"
                this.versionNameSuffix = "-$name"
            }
        }
    }

    buildFeatures.buildConfig = true

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    output.outputFileName = "${rootProject.name}-${output.versionName.get()}-${output.versionCode.get()}.apk"
    afterEvaluate {
        tasks.getByName<KotlinCompile>("compile${variant.name.replaceFirstChar(Character::toUpperCase)}Kotlin") {
            compilerOptions.jvmTarget = JvmTarget.fromTarget(Version.jvmTarget)
        }
    }
}

dependencies {
    implementation(compose.foundation)
    implementation("androidx.activity:activity-compose:1.13.0")
}
