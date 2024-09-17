pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
    id("org.ajoberstar.reckon.settings") version("0.18.3")
}

extensions.configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    snapshots()
    setDefaultInferredScope("patch")
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
}

rootProject.name = "intellij-build123d-viewer-plugin"
