package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.docker.Mount
import com.example.flowkback.app.api.docker.PortForwarding
import com.example.flowkback.utils.mapOrEmpty
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.*
import org.springframework.stereotype.Component
import java.io.File

@Component
class CreateContainerAdapter(private val dockerClient: DockerClient) : CreateContainerOutbound {
    override fun create(
        image: String,
        containerName: String,
        mounts: List<Mount>,
        ports: List<PortForwarding>,
        network: String
    ): String {
        val binds = mounts.mapOrEmpty { Bind(File(it.from).absolutePath, Volume(it.to)) }

        val exposedPorts = ports.mapOrEmpty { ExposedPort(it.containerPort) }
        val portBindings = ports.mapOrEmpty {
            PortBinding(Ports.Binding.bindPort(it.hostPort), ExposedPort(it.containerPort))
        }

        return dockerClient.createContainerCmd(image)
            .withName(containerName)
            .withBinds(*binds.toTypedArray())
            .withExposedPorts(*exposedPorts.toTypedArray())
            .withPortBindings(*portBindings.toTypedArray())
            .withNetworkMode(network)
            .exec()
            .id
    }
}