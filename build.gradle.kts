import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    signing
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("dev.yumi.gradle.licenser") version "4.0.+"
}

val projectVersion = property("project_version").toString()

group = "io.github.toxicity188.javamesh"
version = projectVersion

repositories {
    mavenCentral()
}

signing {
    useGpgCmd()
}

dependencies {
    implementation("org.joml:joml:1.10.9")
    implementation("com.google.code.gson:gson:2.15.0")
    implementation("org.jetbrains:annotations:26.1.0")
    implementation("it.unimi.dsi:fastutil:8.5.19")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

license {
    rule(file("LICENSE_HEADER"))
    include("**/*.java")
    exclude("**/*.properties")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.toxicity188", "java-mesh", projectVersion)
    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = SourcesJar.Sources(),
        )
    )
    pom {
        name = rootProject.name
        description = "A library for converting mesh models for use in Minecraft Java Edition."
        inceptionYear = "2026"
        url = "https://github.com/toxicity188/java-mesh/"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit-license.org/"
            }
        }
        developers {
            developer {
                id = "toxicity188"
                name = "toxicity188"
                url = "https://github.com/toxicity188/"
            }
        }
        scm {
            url = "https://github.com/toxicity188/java-mesh/"
            connection = "scm:git:git://github.com/toxicity188/java-mesh.git"
            developerConnection = "scm:git:ssh://git@github.com/toxicity188/java-mesh.git"
        }
    }
}
