plugins { id("org.jetbrains.kotlin.jvm"); id("org.jetbrains.kotlinx.kover") }
kotlin { jvmToolchain(17) }
dependencies { testImplementation(kotlin("test-junit")) }
