import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.shadow)
}

group = "cc.worldmandia.${project.name}"
version = "1.0.0"

data class TargetSettings(
    val depVersion: String,
    val apiVersion: String,
    val minJdkVersion: String = "1.8"
)

val targetsConfig = mapOf(
    "v1_20_1" to TargetSettings("1.20.1-R0.1-SNAPSHOT", "1.20", "17"),
    "v1_21"   to TargetSettings("1.21.1-R0.1-SNAPSHOT", "1.21", "21")
)

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly("io.papermc.paper:paper-api:${targetsConfig.values.first().depVersion}")
                compileOnly(libs.commandapi)
                compileOnly(libs.commandapi.kotlin)
            }
        }

        targetsConfig.forEach { (targetName, settings) ->
            jvm(targetName) {
                compilations.all { kotlinOptions.jvmTarget = settings.minJdkVersion }
            }
            getByName("${targetName}Main") {
                dependsOn(commonMain)
                dependencies {
                    compileOnly("io.papermc.paper:paper-api:${settings.depVersion}")
                }
            }
        }
    }
}

targetsConfig.forEach { (targetName, settings) ->

    val resourceTaskName = "${targetName}ProcessResources"

    val processResourcesTask = tasks.named<ProcessResources>(resourceTaskName) {
        val props = mapOf(
            "version" to project.version,
            "name" to project.name,
            "apiVersion" to settings.apiVersion,
            "group" to project.group,
            "description" to "KMP plugin for Minecraft servers",
        )

        inputs.properties(props)

        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    val shadowTaskName = "shadowJar${targetName.replaceFirstChar { it.uppercase() }}"

    tasks.register<ShadowJar>(shadowTaskName) {
        group = "build"
        description = "Assembles a shadow jar for $targetName"

        val target = kotlin.targets.getByName(targetName)
        val compilation = target.compilations.getByName("main")

        from(compilation.output)

        val runtimeConfigName = compilation.runtimeDependencyConfigurationName
            ?: "${targetName}MainRuntimeClasspath"
        val runtimeConfig = project.configurations.getByName(runtimeConfigName)

        configurations = listOf(runtimeConfig)

        archiveBaseName.set(project.name)
        archiveClassifier.set(targetName)
        archiveVersion.set(version.toString())

        dependsOn(processResourcesTask)
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds all shadow jars"
    dependsOn(tasks.withType<ShadowJar>().matching { it.name.startsWith("shadowJarV") })
}