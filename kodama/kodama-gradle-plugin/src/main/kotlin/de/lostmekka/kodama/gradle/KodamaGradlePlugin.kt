package de.lostmekka.kodama.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

private const val VERSION = "1.0.0-SNAPSHOT" // TODO

class KodamaGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("kodama", KodamaGradlePluginExtension::class.java)
    }

    override fun getCompilerPluginId(): String = "kodama-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "de.lostmekka.kodama",
            artifactId = "kodama-compiler-plugin",
            version = VERSION,
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(KodamaGradlePluginExtension::class.java)
        val enabled = extension.enabled.get()

        project
            .configurations
            .getByName("implementation")
            .dependencies
            .add(project.dependencies.create("de.lostmekka.kodama:kodama-core:$VERSION"))

        return project.provider {
            listOf(SubpluginOption(key = "enabled", value = enabled.toString()))
        }
    }
}
