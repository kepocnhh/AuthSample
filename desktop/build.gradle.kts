import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose") version Version.compose
    id("org.jetbrains.kotlin.plugin.compose") version Version.kotlin
}

val buildType by properties
val specifics by properties
val platform by properties
val arch by properties

sourceSets {
    getByName("main") {
        kotlin.srcDirs("../shared/src/$name/kotlin")
        setOf(buildType, specifics).forEach { name ->
            kotlin.srcDirs("src/$name/kotlin")
            kotlin.srcDirs("../shared/src/$name/kotlin")
        }
    }
}

tasks.getByName<JavaCompile>("compileJava") {
    targetCompatibility = Version.jvmTarget
}

tasks.getByName<KotlinCompile>("compileKotlin") {
    compilerOptions.jvmTarget = JvmTarget.fromTarget(Version.jvmTarget)
}

fun getTargetFormat(): TargetFormat {
    return when (platform) {
        "macos" -> TargetFormat.Dmg
        else -> error("Platform \"$platform\" is not supported!")
    }
}

version = "1.0.0"

compose.desktop {
    application {
        mainClass = "test.desktop.auth.AppKt" // todo
        var packageName = rootProject.name
        if (buildType != "release") {
            packageName += "-$buildType"
        }
        if (specifics != "real") {
            packageName += "-$specifics"
        }
        nativeDistributions {
            this.packageName = packageName
            this.packageVersion = version as String
            targetFormats = setOf(getTargetFormat())
        }
    }
}

dependencies {
    when (val entry = Pair(platform, arch)) {
        "macos" to "arm64" -> {
            implementation(compose.desktop.macos_arm64)
        }
        else -> {
            val (platform, arch) = entry
            error("Platform \"$platform($arch)\" is not supported!")
        }
    }
}
