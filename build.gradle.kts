plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.annotation)

    // ── Compose 依赖（compileOnly，宿主按需声明）──
    // 不使用 Compose 的宿主 App 不会打包 Compose 运行时（约 2-3MB）
    // 使用 Compose 的宿主 App 需自行声明 Compose 依赖
    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.bundles.compose)

    // ── 测试依赖 ──
    testImplementation(libs.bundles.test)

    // ── Compose Runtime（测试编译+运行时需要）──
    // buildFeatures.compose=true 会全局启用 Compose Compiler 插件，
    // 但 Compose 依赖为 compileOnly，不在测试类路径上。
    // testImplementation 使 Compose 类在测试运行时可用，
    // 修复 MorphColorPalette 等使用 Compose Color 的测试类初始化失败。
    //
    // ── Compose UI 测试依赖 ──
    // BOM 统一版本已在 version catalog 中管理
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.bundles.composeTest)
}
