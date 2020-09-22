rootProject.name = "appChecker"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                val vKotlin: String by settings
                useVersion(vKotlin)
            }
        }
    }
}