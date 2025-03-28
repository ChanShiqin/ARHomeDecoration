plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.arhomedecorationapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.arhomedecorationapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Use the BOM for Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    implementation("com.google.firebase:firebase-database-ktx") // No version number
    implementation("com.google.firebase:firebase-auth-ktx") // No version number
    implementation("com.google.firebase:firebase-analytics") // No version number
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("com.google.firebase:firebase-auth:21.0.6")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.github.bumptech.glide:glide:4.14.2")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.picasso:picasso:2.71828")

//  AR
    implementation("com.gorisse.thomas.sceneform:sceneform:1.20.1")
    implementation("androidx.appcompat:appcompat:1.3.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    implementation("com.stripe:stripe-android:20.35.0")

    implementation("com.github.dhaval2404:imagepicker:2.1")

//
//    implementation("io.github.sceneview:sceneview:2.2.1")  // Sceneview AR Library
//
//    // For ARCore (AR functionality)
//    implementation("com.google.ar:core:1.43.0")  // ARCore
//
//    // For AR Toolkit (optional, but might help with SceneView)
//    implementation("io.github.sceneview:sceneview-ar:2.2.1")



}