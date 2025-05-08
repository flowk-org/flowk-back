package com.example.flowkback.app.impl

import com.example.flowkback.app.api.executor.PipelineExecutor
import com.example.flowkback.domain.run.StageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class DefaultPipelineExecutor(
    private val trainModelUseCase: TrainModelUseCase,
    private val evaluateModelUseCase: EvaluateModelUseCase
) : PipelineExecutor {

    private val logger = LoggerFactory.getLogger(DefaultPipelineExecutor::class.java)

    private val scheduledPipelines: MutableMap<String, List<StageType>> = mutableMapOf()

    override fun schedule(projectName: String, pipelines: List<StageType>) {
        logger.info("Scheduling pipelines $pipelines for project: $projectName")
        scheduledPipelines[projectName] = pipelines
    }

    override fun start(projectName: String, pipelines: List<StageType>) {
        logger.info("Starting pipelines $pipelines for project: $projectName")

        pipelines.forEach { pipeline ->
            try {
                when (pipeline) {
                    StageType.TRAIN -> {
                        val trainScript = Paths.get("repos/$projectName/train.py").toFile()
                        if (!trainScript.exists()) {
                            logger.warn("No train.py found for project: $projectName — skipping TRAIN pipeline")
                            return@forEach
                        }

                        trainModelUseCase.execute(
                            trainScript,
                            projectName,
                            "/models",
                            "3.10"
                        )

                        logger.info("TRAIN pipeline completed for $projectName")
                    }

                    StageType.TEST -> {
                        val testScript = Paths.get("repos/$projectName/test.py").toFile()
                        if (!testScript.exists()) {
                            logger.warn("No test.py found for project: $projectName — skipping TRAIN pipeline")
                            return@forEach
                        }

                        evaluateModelUseCase.execute(
                            testScript,
                            projectName,
                            "/models",
                            "/metrics",
                            "3.10"
                        )

                        logger.info("TEST pipeline completed for $projectName")
                    }

                    StageType.PREPARE -> {

                    }

                    StageType.DEPLOY -> {

                    }
                }
            } catch (ex: Exception) {
                logger.error("Failed to execute pipeline $pipeline for project: $projectName", ex)
            }
        }
    }
}
