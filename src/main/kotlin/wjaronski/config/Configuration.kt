package wjaronski.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import wjaronski.config.dto.Settings
import java.nio.file.Files
import java.nio.file.Path

class Configuration private constructor() {

    companion object {
        operator fun invoke(): Configuration {
            return Configuration()
        }
    }

    val settings: Settings = loadSettingsFromResources("settings.yaml")

    private fun loadSettingsFromResources(filename: String): Settings {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())

        return Files.newBufferedReader(Path.of(ClassLoader.getSystemResource(filename).toURI())).use {
            mapper.readValue(it, Settings::class.java)
        }
    }
}