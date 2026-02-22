plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.2.1"
}

group = "com.dopamine"
version = "1.1.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.dopamine.cobblemontrainermaker")
    mainClass.set("com.dopamine.cobblemontrainermaker.TrainerApplication")
}

javafx {
    version = "21.0.10"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
      exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "CobblemonTrainerMaker"
    }
}

// Wraps the jlink image into a native app folder with a .exe launcher
tasks.register<Exec>("packageApp") {
    dependsOn("jlink")
    val jpackageBin = "${System.getProperty("java.home")}/bin/jpackage"
    val imageDir = layout.buildDirectory.dir("image").get().asFile.absolutePath
    val destDir  = layout.buildDirectory.dir("dist").get().asFile.absolutePath
    doFirst {
        file("$destDir/CobblemonTrainerMaker").deleteRecursively()
        file(destDir).mkdirs()
    }
    commandLine(
        jpackageBin,
        "--type",          "app-image",
        "--name",          "CobblemonTrainerMaker",
        "--runtime-image", imageDir,
        "--module",        "com.dopamine.cobblemontrainermaker/com.dopamine.cobblemontrainermaker.TrainerApplication",
        "--dest",          destDir
    )
}