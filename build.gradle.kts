// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
}

val srcDir = file("app/src/main")

tasks.register("preprocessJava") {
    doLast {

        println("=== Running cpp and cleaning output ===")

        fileTree(srcDir).matching {
            include("**/*.cava")
        }.files.forEach { f ->
            val output = File(f.parentFile, f.name.replace(".cava", ".java"))

            // Run cpp
            exec {
                commandLine("cpp", f.absolutePath, output.absolutePath)
            }

            // Remove lines starting with #
            val cleaned = output.readLines().filter { !it.trim().startsWith("#") }
            output.writeText(cleaned.joinToString(System.lineSeparator()))
        }
    }
}