package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.BuildImageOutbound
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.BuildResponseItem
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.CompletableFuture

@Component
class BuildImageAdapter(private val dockerClient: DockerClient): BuildImageOutbound {
    override fun build(dockerfileDir: File, imageName: String): String {
        val imageIdFuture = CompletableFuture<String>()

        dockerClient.buildImageCmd()
            .withDockerfile(dockerfileDir)
            .withTags(setOf(imageName))
            .exec(buildCallback(imageIdFuture))

        val imageId = imageIdFuture.get()
        println("Image built with ID: $imageId")
        return imageId
    }

    private fun buildCallback(imageIdFuture: CompletableFuture<String>): ResultCallback.Adapter<BuildResponseItem> {
        return object : ResultCallback.Adapter<BuildResponseItem>() {
            override fun onNext(item: BuildResponseItem) {
                item.stream?.let { print(it) }
                item.imageId?.let { imageId ->
                    if (!imageIdFuture.isDone) {
                        imageIdFuture.complete(imageId)
                    }
                }
            }

            override fun onError(throwable: Throwable?) {
                imageIdFuture.completeExceptionally(throwable ?: RuntimeException("Unknown error during image build"))
            }

            override fun onComplete() {
                if (!imageIdFuture.isDone) {
                    imageIdFuture.completeExceptionally(RuntimeException("Image ID not found in build output"))
                }
            }
        }
    }
}