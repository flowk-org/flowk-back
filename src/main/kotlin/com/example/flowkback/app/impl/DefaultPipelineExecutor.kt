package com.example.flowkback.app.impl

import com.example.flowkback.app.api.executor.PipelineExecutor
import com.example.flowkback.domain.run.StageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class DefaultPipelineExecutor(
    private val prepareDataUseCase: PrepareDataUseCase,
    private val trainModelUseCase: TrainModelUseCase,
    private val evaluateModelUseCase: EvaluateModelUseCase,
    private val deployModelUseCase: DeployModelUseCase
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
                    StageType.PREPARE -> {
                        val migrationDir = Paths.get("repos/$projectName/db/migrations").toFile()
                        if (!migrationDir.exists()) {
                            logger.warn("No migraions found for project: $projectName — skipping PREP pipeline")
                            return@forEach
                        }

                        prepareDataUseCase.execute(projectName, migrationDir.absolutePath)

                        logger.info("PREP pipeline completed for $projectName")
                    }

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

                    StageType.DEPLOY -> {
                        val servingScript = Paths.get("repos/$projectName/serving.py").toFile()
                        if (!servingScript.exists()) {
                            logger.warn("No test.py found for project: $projectName — skipping TRAIN pipeline")
                            return@forEach
                        }

                        deployModelUseCase.execute(
                            servingScript,
                            projectName,
                            "/models",
                            "3.10"
                        )

                        logger.info("DEPLOY pipeline completed for $projectName")
                    }
                }
            } catch (ex: Exception) {
                logger.error("Failed to execute pipeline $pipeline for project: $projectName", ex)
            }
        }
    }
}
