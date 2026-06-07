plugins {
    id("com.android.library") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.morphkit"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = false
        dataBinding = false
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
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.2")

    // ── Compose Runtime（仅测试编译需要）──
    // buildFeatures.compose=true 会全局启用 Compose Compiler 插件，
    // 但 Compose 依赖为 compileOnly，不在测试类路径上。
    // 添加 testCompileOnly 使 Compose Compiler 在测试编译时能找到 Runtime。
    testCompileOnly(platform("androidx.compose:compose-bom:2025.05.01"))
    testCompileOnly("androidx.compose.runtime:runtime")
}
