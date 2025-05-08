package com.example.flowkback.utils

import com.example.flowkback.domain.config.Config
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
object ConfigParser {
    private val mapper = ObjectMapper(YAMLFactory()).apply {
        registerKotlinModule()
        findAndRegisterModules()
    }

    fun parseFromFile(path: String): Config {
        return mapper.readValue(File(path), Config::class.java)
    }
}