plugins {
    kotlin("multiplatform")
}

group = "org.phakfasu"
version = "0.1.0"

base {
    archivesName.set("konvert-to-pfs")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "KonvertToPFS"
        }
    }

    js {
        browser()
        nodejs()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {}
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}