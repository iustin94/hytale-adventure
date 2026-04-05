plugins {
    java
}

group = "dev.hytalemodding"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/release/")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:latest.release")
    compileOnly(files("../hytale-plugin/build/libs/dev.hytalemodding.jar"))
    implementation("com.google.code.gson:gson:2.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("hytale-adventure")
    archiveVersion.set(version.toString())
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.build {
    dependsOn("fatJar")
}
