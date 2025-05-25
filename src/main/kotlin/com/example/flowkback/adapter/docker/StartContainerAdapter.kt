package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.StartContainerOutbound
import com.github.dockerjava.api.DockerClient
import org.springframework.stereotype.Component

@Component
class StartContainerAdapter(private val dockerClient: DockerClient): StartContainerOutbound{
    override fun start(containerId: String): String {
        dockerClient.startContainerCmd(containerId).exec()
        return "Container with ID: $containerId started."
    }
}
