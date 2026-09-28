pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "ProyectoLince"
include(":dominio")
// Permite practicar y probar las reglas Kotlin sin instalar Android.
if (!providers.gradleProperty("soloDominio").isPresent) include(":app")
