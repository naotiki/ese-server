import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val kotlinVersion = "1.8.21"
val serializationVersion = "1.5.0"
val ktorVersion = "2.3.0"
val kotlinWrappersVersion = "1.0.0-pre.547"//545"

plugins {
    kotlin("multiplatform") version "1.8.21"
    application //to run JVM part
    kotlin("plugin.serialization") version "1.8.21"
}

group = "me.naotiki.ese"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"
kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")
                implementation("io.ktor:ktor-server-compression:$ktorVersion")
                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-resources:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
                implementation("io.ktor:ktor-server-http-redirect:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
                implementation("io.ktor:ktor-client-apache:$ktorVersion")

                val koinVersion = "3.4.0"
                compileOnly("io.insert-koin:koin-core:$koinVersion")
                implementation("io.insert-koin:koin-ktor:$koinVersion")
                implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

                implementation("ch.qos.logback:logback-classic:1.4.7")
                implementation("software.amazon.awssdk:s3:2.20.56")

                val exposedVersion="0.41.1"
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("com.h2database:h2:2.1.214")
                implementation("mysql:mysql-connector-java:8.0.33")

            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation(project.dependencies.enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
                implementation(kotlinw("react"))
                implementation(kotlinw("react-dom"))
                implementation(kotlinw("emotion"))
                implementation(kotlinw("react-router-dom"))
                implementation(kotlinw("mui"))
                implementation(kotlinw("mui-icons"))
            }
        }
    }
}

application {
    mainClass.set("ServerKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

// include JS artifacts in any JAR we generate
tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")
        || project.gradle.startParameter.taskNames.contains("installDist")
    ) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

// Alias "installDist" as "stage" (for cloud providers)
tasks.create("stage") {
    dependsOn(tasks.getByName("installDist"))
}

tasks.getByName<JavaExec>("run") {
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}
