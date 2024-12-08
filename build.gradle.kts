plugins {
    application
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.com.code.intelligence.jazzer.api)
    implementation(libs.javax.json.javax.json.api)
    implementation(libs.org.glassfish.javax.json)
    implementation(libs.org.jetbrains.annotations)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

application {
    mainClass.set("com.company.maze")
}
