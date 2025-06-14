plugins {
    id("java")
    id ("com.gradleup.shadow") version "8.3.0"
}

group = "dev.mikan"
version = "1.0"

val outputDir = file("/home/mikan/Desktop/builds")

tasks.register<Copy>("copy"){
    dependsOn(tasks.named("jar"))
    from(tasks.named("jar").get().outputs.files)
    into(outputDir)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>(){
    archiveClassifier.set("")
    relocate("dev.mikan.altairkit", "dev.mikan.shaded.altairkit")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenLocal()
    mavenCentral()
    flatDir {
        dirs ("/home/mikan/.m2/repository/dev/mikan/AltairKit/1.8.8")
    }

    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly ("org.projectlombok:lombok:1.18.36")
    annotationProcessor ("org.projectlombok:lombok:1.18.36")

    // Packet events
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    implementation("dev.mikan:AltairKit:1.8.8")
}

tasks.named("build"){
    dependsOn(tasks.named("shadowJar"))
    finalizedBy("copy")

}
