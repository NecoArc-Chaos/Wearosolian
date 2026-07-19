import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.BaseExtension


val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}
subprojects {
    plugins.withId("com.android.library") {
        extensions.findByName("android")?.let { ext ->
            if (ext is BaseExtension) {
                ext.compileSdkVersion(36)
                if (ext.namespace == null) {
                    val manifest = project.file("src/main/AndroidManifest.xml")
                    if (manifest.exists()) {
                        val matcher = java.util.regex.Pattern.compile("package=\"([^\"]+)\"").matcher(manifest.readText())
                        if (matcher.find()) {
                            ext.namespace = matcher.group(1)
                        }
                    }
                }
            }
        }
    }

    plugins.withId("com.android.application") {
        extensions.findByName("android")?.let { ext ->
            if (ext is BaseExtension) {
                ext.compileSdkVersion(36)
            }
        }
    }

    val overrideCompileSdk = {
        plugins.withId("com.android.library") {
            extensions.findByName("android")?.let { ext ->
                if (ext is BaseExtension) {
                    ext.compileSdkVersion(36)
                }
            }
        }
        plugins.withId("com.android.application") {
            extensions.findByName("android")?.let { ext ->
                if (ext is BaseExtension) {
                    ext.compileSdkVersion(36)
                }
            }
        }
    }

    if (project.state.executed) {
        overrideCompileSdk()
    } else {
        project.afterEvaluate { overrideCompileSdk() }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
