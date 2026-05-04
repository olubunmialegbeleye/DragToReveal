plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

android {
    namespace = "io.github.olubunmialegbeleye.dragtoreveal"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId    = property("GROUP").toString()
                artifactId = property("POM_ARTIFACT_ID").toString()
                version    = property("VERSION_NAME").toString()

                pom {
                    name.set(property("POM_NAME").toString())
                    description.set(property("POM_DESCRIPTION").toString())
                    url.set(property("POM_URL").toString())

                    licenses {
                        license {
                            name.set(property("POM_LICENSE_NAME").toString())
                            url.set(property("POM_LICENSE_URL").toString())
                        }
                    }

                    developers {
                        developer {
                            id.set(property("POM_DEVELOPER_ID").toString())
                            name.set(property("POM_DEVELOPER_NAME").toString())
                        }
                    }

                    scm {
                        url.set(property("POM_SCM_URL").toString())
                    }
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url  = uri("https://maven.pkg.github.com/yourusername/DragToReveal")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}