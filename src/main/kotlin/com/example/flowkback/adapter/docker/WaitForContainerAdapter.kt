package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.WaitForContainerOutbound
import com.github.dockerjava.api.DockerClient
import org.springframework.stereotype.Component

@Component
class WaitForContainerAdapter(private val dockerClient: DockerClient) : WaitForContainerOutbound {
    override fun wait(containerId: String): Int {
        return dockerClient.waitContainerCmd(containerId)
            .start()
            .awaitStatusCode()
    }
}