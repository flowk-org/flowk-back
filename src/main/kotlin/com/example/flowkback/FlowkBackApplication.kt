package com.example.flowkback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File

@SpringBootApplication
@EnableScheduling
class FlowkBackApplication

fun main(args: Array<String>) {
	runApplication<FlowkBackApplication>(*args)
//	val dockerResult = DockerRunner.run("python:3.10", listOf("python", "-c", "print('Hello from Docker')"))
//	println("Docker logs:\n${dockerResult.logs}")
}
