package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.docker.Mount
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import org.springframework.stereotype.Component
import java.io.File

@Component
class CreateContainerAdapter(private val dockerClient: DockerClient) : CreateContainerOutbound {
    override fun create(
        image: String,
        containerName: String,
        mounts: List<Mount>
    ): String {
        val binds: List<Bind> = if (mounts.isEmpty())
            listOf()
        else mounts.map {
            Bind(File(it.from).absolutePath, Volume(it.to))
        }

        return dockerClient.createContainerCmd(image)
            .withName(containerName)
            .withBinds(*binds.toTypedArray())
            .exec()
            .id
    }
}