plugins {
    id("com.android.library") version "8.9.1"
    id("org.jetbrains.kotlin.android") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
}

android {
    namespace = "com.morphkit"
    compileSdk = 35

    defaultConfig {
        minSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        targetSdk = 35
        unitTests {
            // 让 android.util.Log 等 Android API 返回默认值而非抛异常
            isReturnDefaultValues = true
        }
    }
}

// 启用 JUnit5 平台支持（MorphKitTest 使用 JUnit5）
tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    // ── Android 基础依赖（始终包含）──
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.annotation:annotation:1.9.1")

    // ── Compose 依赖（compileOnly，宿主按需声明）──
    // 不使用 Compose 的宿主 App 不会打包 Compose 运行时（约 2-3MB）
    // 使用 Compose 的宿主 App 需自行声明 Compose 依赖
    compileOnly(platform("androidx.compose:compose-bom:2025.05.01"))
    compileOnly("androidx.compose.material3:material3")
    compileOnly("androidx.compose.ui:ui")
    compileOnly("androidx.compose.ui:ui-text")
    compileOnly("androidx.compose.foundation:foundation")

    // ── 测试依赖 ──
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.21")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.2")

    // ── Compose Runtime（测试编译+运行时需要）──
    // buildFeatures.compose=true 会全局启用 Compose Compiler 插件，
    // 但 Compose 依赖为 compileOnly，不在测试类路径上。
    // testImplementation 使 Compose 类在测试运行时可用，
    // 修复 MorphColorPalette 等使用 Compose Color 的测试类初始化失败。
    testImplementation(platform("androidx.compose:compose-bom:2025.05.01"))
    testImplementation("androidx.compose.runtime:runtime")

    // ── Compose UI 测试依赖 ──
    testImplementation(platform("androidx.compose:compose-bom:2025.05.01"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.compose.material3:material3")
    testImplementation("androidx.compose.ui:ui-test-manifest")
}
