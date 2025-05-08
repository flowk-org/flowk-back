package com.example.flowkback.adapter.kafka

import com.example.flowkback.app.api.executor.PipelineExecutor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PipelineListener(
    private val pipelineExecutor: PipelineExecutor,
    private val objectMapper: ObjectMapper
) {

//    @KafkaListener(topics = ["stage.started"], groupId = "pipeline-executor")
//    fun onStageStarted(message: String) {
//        val event = objectMapper.readValue(message, StageStartedEvent::class.java)
//        println("🔧 Stage started: ${event.stage.name} for pipeline ${event.pipelineId}")
//        // Запуск стадии в StageExecutor
//        pipelineExecutor.executeNextStage(event.pipelineId)
//    }
//
//    @KafkaListener(topics = ["stage.completed"], groupId = "pipeline-executor")
//    fun onStageCompleted(message: String) {
//        val event = objectMapper.readValue(message, StageCompletedEvent::class.java)
//        println("✅ Stage completed: ${event.stage.name} for pipeline ${event.pipelineId}")
//        // Запуск следующей стадии
//        pipelineExecutor.executeNextStage(event.pipelineId)
//    }
//
//    @KafkaListener(topics = ["stage.failed"], groupId = "pipeline-executor")
//    fun onStageFailed(message: String) {
//        val event = ObjectMapper().readValue(message, StageFailedEvent::class.java)
//        println("❌ Stage failed: ${event.stage.name} for pipeline ${event.pipelineId}. Error: ${event.error}")
//        // Обработка ошибки, например, завершение пайплайна или повтор
//    }
}
