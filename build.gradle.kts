plugins {
    kotlin("jvm") version "1.8.20"
    id("maven-publish")
}

group = "org.tera201"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "GitHub"
        url = uri("https://maven.pkg.github.com/tera201/Code2UML")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: ""
            password = System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
}

dependencies {
    implementation("org.antlr:antlr4:4.13.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation("org.apache.logging.log4j:log4j-core:2.12.4")

    implementation("com.fasterxml.jackson.core:jackson-core:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.1")

    // Local JAR dependency (equivalent to <scope>system</scope>)
    implementation(files("lib/CloudUML-1.0.0.jar"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}