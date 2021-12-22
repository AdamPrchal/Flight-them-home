package entities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class FTHConfiguration(val planes: Array<Plane>)

data class Configuration(val fth: FTHConfiguration)


fun loadFromFile(): Configuration {

    val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
    mapper.registerModule(KotlinModule()) // Enable Kotlin support
    val path = Paths.get("application.yaml")
    return Files.newBufferedReader(path).use {
        mapper.readValue(it, Configuration::class.java)
    }
}