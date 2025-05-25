package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.RemoveContainerOutbound
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.RemoveContainerCmd
import org.springframework.stereotype.Component

@Component
class RemoveContainerAdapter(private val dockerClient: DockerClient): RemoveContainerOutbound {

    override fun remove(containerId: String): String {
        val removeContainerCmd: RemoveContainerCmd = dockerClient.removeContainerCmd(containerId)
        removeContainerCmd.exec()
        return "Container with ID: $containerId removed."
    }
}
