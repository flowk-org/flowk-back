package com.example.flowkback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class FlowkBackApplication

fun main(args: Array<String>) {
	runApplication<FlowkBackApplication>(*args)
//	val dockerResult = DockerRunner.run("python:3.10", listOf("python", "-c", "print('Hello from Docker')"))
//	println("Docker logs:\n${dockerResult.logs}")
}
