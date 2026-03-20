import sp.kx.gradlex.buildDir
import sp.kx.gradlex.buildSrc

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
    }
}

tasks.register<Delete>("clean") {
    delete = setOf(buildDir(), buildSrc.buildDir())
}
