import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "io.github.tomaspecserke.intellij.plugins"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        pycharmCommunity("2024.2.1")
        bundledPlugin("PythonCore")
        instrumentationTools()
    }

    testImplementation(kotlin("test"))
}

intellijPlatform {
    projectName = project.name

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242.21829.153"
        }

        vendor {
            name = "Tomáš Pecsérke"
            email = "tomas.pecserke@proton.me"
            url = "https://github.com/tomas-pecserke/"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.WARNING)
}

tasks.compileJava.configure {
    targetCompatibility = "17"
}

tasks.compileKotlin.configure {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
