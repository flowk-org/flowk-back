package com.example.flowkback.utils

import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.okhttp.OkDockerHttpClient


object DockerRunner {

    data class RunResult(val success: Boolean, val logs: String, val exitCode: Int)

    fun run(image: String, command: List<String>, name: String = "ml-job"): RunResult {


        val config: DockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerTlsVerify(false)
            .build()

        val httpClient = OkDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .build()

        val docker = DockerClientImpl.getInstance(config, httpClient);
//        docker.pullImageCmd("python")
//            .withTag("3.10")
//            .start()
//            .awaitCompletion()

        println("Images downloaded")

        val container = docker.createContainerCmd(image)
            .withName("$name-${System.currentTimeMillis()}")
            .withCmd(command)
            .exec()

        docker.startContainerCmd(container.id).exec()

        val logBuilder = StringBuilder()
        docker.logContainerCmd(container.id)
            .withStdOut(true)
            .withStdErr(true)
            .exec(object : Adapter<Frame>() {
                override fun onNext(item: Frame) {
                    logBuilder.append(String(item.payload))
                }
            })

        val exitCode = docker.waitContainerCmd(container.id).start().awaitStatusCode()
//        docker.removeContainerCmd(container.id).exec()

        return RunResult(
            success = exitCode == 0,
            logs = logBuilder.toString(),
            exitCode = exitCode
        )
    }
}
