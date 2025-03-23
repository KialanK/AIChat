plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("maven-publish")
}

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val mcVersion = stonecutter.current.version

repositories {
    // Add repositories to retrieve artifacts from in here.
}

version = "${property("mod_version")}[Fabric$mcVersion]"
group = project.property("maven_group") as String

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    implementation("com.google.code.gson:gson:2.8.9")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcVersion", mcVersion)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version, "mcVersion" to mcVersion))
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val jar by tasks.getting(Jar::class) {
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }
    repositories {
        // Add repositories to publish to here.
    }
}
