import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("org.openjfx.javafxplugin") version "0.0.10"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "org.cameek"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.projectreactor:reactor-core:3.4.23")
    implementation("org.apache.sshd:sshd-core:2.9.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Customize Source Paths
// ----------------------
// 1. Setting all orig source dirs to empty list
// 2. Defining for Kotlin sources: Java + Kotlin + Resources
//  => This is needed to support mixed mode for both Java and Kotlin
// Ref.: https://stackoverflow.com/questions/38131237/mixing-java-and-kotlin-in-gradle-project-kotlin-cannot-find-java-class
sourceSets {
    main {
        java.srcDirs()
        resources.srcDirs()
    }
}

kotlin.sourceSets {
    main {
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}