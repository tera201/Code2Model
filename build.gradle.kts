plugins {
    alias(libs.plugins.kotlin)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.antlr)
    implementation(libs.sqlite)
    implementation(libs.slf4j.simple)
}