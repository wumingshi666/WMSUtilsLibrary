@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("maven-publish") // 引入 maven 插件
}


/*publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.wumingshi666"
            artifactId = "WMSUtilsLibrary"
            version = libs.versions.versionName.get()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}*/

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.github.wumingshi666"
                artifactId = "WMSUtilsLibrary"
                version = libs.versions.versionName.get()

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}

android {
    publishing {
        singleVariant("release")
    }
    /*publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
        *//*只发布release版本
        同时发布release版本的源码包
        同时发布release版本的Javadoc文档包*//*
    }*/

    namespace = "com.wumingshi.wmsutilslibrary"
    compileSdk = 34


    defaultConfig {
        minSdk = 24
        version = libs.versions.versionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}