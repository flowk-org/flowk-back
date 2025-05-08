package com.example.flowkback.adapter.docker

import com.example.flowkback.app.api.docker.StreamLogsOutbound
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class StreamLogsAdapter(private val dockerClient: DockerClient): StreamLogsOutbound {
    override fun stream(containerId: String): Flow<String> = callbackFlow {
        val callback = object : ResultCallback.Adapter<Frame>() {
            override fun onNext(frame: Frame) {
                val line = String(frame.payload, StandardCharsets.UTF_8).trimEnd()
                trySend(line).isSuccess
            }

            override fun onError(t: Throwable?) {
                close(t)
            }

            override fun onComplete() {
                close()
            }
        }

        dockerClient.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withFollowStream(true)
            .exec(callback)

        awaitClose { callback.close() }
    }
}
