package com.example.flowkback

import com.example.flowkback.app.impl.TrainModelUseCase
import com.example.flowkback.utils.DockerRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlowkBackApplication

fun main(args: Array<String>) {
	runApplication<FlowkBackApplication>(*args)
//	val dockerResult = DockerRunner.run("python:3.10", listOf("python", "-c", "print('Hello from Docker')"))
//	println("Docker logs:\n${dockerResult.logs}")
}
