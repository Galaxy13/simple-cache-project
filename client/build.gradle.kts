version = "0.1.3-test"

plugins {
    id ("maven-publish")
}

dependencies {
    implementation ("ch.qos.logback:logback-classic")
    implementation ("io.netty:netty-all")

    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine")
    testImplementation ("org.assertj:assertj-core")
    testImplementation ("org.mockito:mockito-core")
    testImplementation ("org.mockito:mockito-junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.galaxy13.cache"
            artifactId = project.name
            version = project.version.toString()
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.register("printPublishedArtifactName") {
    doLast {
        val artifactGroupId = "com.galaxy13.cache" // Set this according to your groupId
        val artifactId = project.name // This is already set in your configuration
        val artifactVersion = project.version.toString() // This is already set in your configuration
        val fullArtifactName = "$artifactGroupId:$artifactId:$artifactVersion"
        println("Full name of the published artifact: $fullArtifactName")
    }
}

// To make sure this runs after the publication has completed, you can configure the task dependency
tasks.named("publish") {
    finalizedBy("printPublishedArtifactName")
}