import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import kotlin.reflect.KProperty

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    alias(libs.plugins.compose)
}

class GradlePropertiesDelegate(
    private val providers: ProviderFactory
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): String = providers.gradleProperty(property.name).get()
}

val ProviderFactory.allGradleProperties: GradlePropertiesDelegate
    get() = GradlePropertiesDelegate(this)

val gradleProperties = providers.allGradleProperties

val pluginGroup by gradleProperties
val pluginVendor by gradleProperties
val pluginVendorName by gradleProperties
val pluginVendorUrl by gradleProperties
val pluginVendorEmail by gradleProperties
val pluginVendorId by gradleProperties
val pluginVersion by gradleProperties
val pluginSinceBuild by gradleProperties
val pluginUntilBuild by gradleProperties
val platformType by gradleProperties
val platformVersion by gradleProperties
val platformBundledPlugins by gradleProperties
val platformPlugins by gradleProperties
val platformBundledModules by gradleProperties


group = pluginGroup
version = pluginVersion

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
    intellijPlatform {
        create(platformType, platformVersion)
        bundledPlugins(platformBundledPlugins.split(','))
        plugins(platformPlugins.split(','))
        bundledModules(platformBundledModules.split(','))
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
    // classes scan
    implementation("io.github.classgraph:classgraph:4.8.180")
    // pdf
    implementation("dev.zt64:compose-pdf:1.2.0")
    // markdown
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.29.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m2:0.29.0")
//    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.29.0")
    // adb
    implementation("dev.mobile:dadb:1.2.10")
    // tests
    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
}

intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    patchPluginXml {
        pluginId = pluginVendorId
        pluginName = pluginVendorName
        sinceBuild = pluginSinceBuild
        untilBuild = pluginUntilBuild
        vendorName = pluginVendor
        vendorUrl = pluginVendorUrl
        vendorEmail = pluginVendorEmail
    }
}
