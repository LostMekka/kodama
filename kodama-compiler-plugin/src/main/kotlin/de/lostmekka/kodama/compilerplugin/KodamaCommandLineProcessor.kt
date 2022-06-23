package de.lostmekka.kodama.compilerplugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_ENABLED = CompilerConfigurationKey<Boolean>("enabled")

@AutoService(CommandLineProcessor::class)
class KodamaCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "kodama-compiler-plugin"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption("enabled", "<true | false>", "", required = true),
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> error("Unknown plugin option: ${option.optionName}")
    }
}
