plugins {
    id("java")
    id ("com.gradleup.shadow") version "8.3.0"
}

group = "dev.mikan"
version = "3.1"

val outputDir = file("/home/mikan/Desktop/localhosts/MemoriaTest/plugins")

tasks.register<Copy>("copy"){
    dependsOn(tasks.named("jar"))
    from(tasks.named("jar").get().outputs.files)
    into(outputDir)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>(){
    archiveClassifier.set("")
    relocate("dev.mikan.altairkit", "dev.mikan.shaded.altairkit")
//    relocate("com.github.retrooper.packetevents", "dev.mikan.shaded.packetevents.api")
//    relocate("io.github.retrooper.packetevents", "dev.mikan.shaded.packetevents.imp")
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
        dirs ("/home/mikan/.m2/repository/com/massivecraft/FactionUUID/1.8.8")
    }

    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "rosewood"
        url = uri("https://repo.rosewooddev.io/repository/public/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly ("org.projectlombok:lombok:1.18.36")
    annotationProcessor ("org.projectlombok:lombok:1.18.36")

    // Packet events
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    implementation("dev.mikan:AltairKit:1.8.8")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // Hikari
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.xerial:sqlite-jdbc:3.50.1.0")

    // Factions
    compileOnly("com.massivecraft:FactionUUID:1.8.8@jar")
    compileOnly("Shampaggon:CrackShot:0.98.11U")

    // Decent holograms
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.9")

    // Economy and points
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("org.black_ixx:playerpoints:3.3.0")
}

tasks.named("build"){
    dependsOn(tasks.named("shadowJar"))
    finalizedBy("copy")

}
