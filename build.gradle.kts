plugins {
    kotlin("jvm") version "2.1.0"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.antlr:antlr4:4.13.0")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.apache.logging.log4j:log4j-core:2.12.4")
}