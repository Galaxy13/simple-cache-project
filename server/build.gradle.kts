import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

version = "0.1.1-test"

plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation ("ch.qos.logback:logback-classic")
    implementation ("io.netty:netty-all")
    implementation("commons-cli:commons-cli")

    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine")
    testImplementation ("org.assertj:assertj-core")
    testImplementation ("org.mockito:mockito-core")
    testImplementation ("org.mockito:mockito-junit-jupiter")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("server")
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Main-Class" to "com.galaxy13.Server"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
