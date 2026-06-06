plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.morphkit"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34

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
    }
}

dependencies {
    // ═══════════════════════════════════════════════════════════════════════
    //  核心依赖 — AppCompat 兼容层 + Material Design 组件
    // ═══════════════════════════════════════════════════════════════════════
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // ═══════════════════════════════════════════════════════════════════════
    //  注解支持
    // ═══════════════════════════════════════════════════════════════════════
    implementation("androidx.annotation:annotation:1.7.1")

    // ═══════════════════════════════════════════════════════════════════════
    //  测试依赖
    // ═══════════════════════════════════════════════════════════════════════
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
}
