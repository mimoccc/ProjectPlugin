import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get()
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    intellijPlatform {
        defaultRepositories()
    }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://plugins.gradle.org/m2/")
    google()
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion")
        )
        bundledPlugins(
            providers.gradleProperty(
                "platformBundledPlugins"
            ).map {
                it.split(',')
            })
        plugins(
            providers.gradleProperty("platformPlugins").map {
                it.split(',')
            }
        )
        bundledModules(
            providers.gradleProperty("platformBundledModules").map {
                it.split(',')
            }
        )
        testFramework(TestFrameworkType.Platform)
    }
    // compose
    implementation(libs.compose.desktop)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    // json
    implementation("org.json:json:20240303")
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.20")
    // scripting kotlin embedded
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:2.2.20")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:2.2.20")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:2.2.20")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:2.2.20")
    // skiko
    runtimeOnly("org.jetbrains.skiko:skiko-awt:0.9.4")
    runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.9.4")
    runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.9.4")
}
