package com.example.flowkback.adapter.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.RemoveContainerCmd
import com.github.dockerjava.api.command.StartContainerCmd
import com.github.dockerjava.api.command.WaitContainerCmd
import com.github.dockerjava.api.model.Frame
import org.springframework.stereotype.Component

@Component
class DockerAdapter(private val dockerClient: DockerClient) {
    fun startContainer(containerId: String): String {
        dockerClient.startContainerCmd(containerId).exec()
        return "Container with ID: $containerId started."
    }

    fun waitForContainer(containerId: String): Int {
        val waitContainerCmd: WaitContainerCmd = dockerClient.waitContainerCmd(containerId)
        return waitContainerCmd.start().awaitStatusCode()
    }

    fun removeContainer(containerId: String): String {
        val removeContainerCmd: RemoveContainerCmd = dockerClient.removeContainerCmd(containerId)
        removeContainerCmd.exec()
        return "Container with ID: $containerId removed."
    }

    fun getContainerLogs(containerId: String): String {
        val logBuilder = StringBuilder()

        dockerClient.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(item: Frame) {
                    logBuilder.append(String(item.payload))
                }
            })

        return logBuilder.toString()
    }

    fun removeVolume(volumeName: String) {
        dockerClient.removeVolumeCmd(volumeName).exec()
    }
}
