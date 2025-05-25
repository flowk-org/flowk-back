package com.example.flowkback.fw

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.domain.project.Project
import com.example.flowkback.domain.run.Run
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.Stage
import com.example.flowkback.domain.run.StageType
import com.example.flowkback.utils.ConfigParser
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class DataInitializer {

    @Bean
    fun initRuns(runRepository: RunRepository, projectRepository: ProjectRepository) = CommandLineRunner {
        runRepository.deleteAll()
        projectRepository.deleteAll()
        val project = projectRepository.save(
            Project(
                name = "flowk-test",
                gitUrl = "http://localhost:3003/Diachenko/flowk-test.git",
                config = ConfigParser.parseFromFile("./repos/flowk-test/mlci.yaml")
            )
        )
        val projectId = project.id.orEmpty()

        if (runRepository.count() == 0L) {
            println("Инициализация коллекции runs")

            val runs = listOf(
                Run(
                    runNumber = 8,
                    projectId = projectId,
                    status = RunStatus.RUNNING,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.RUNNING),
                        Stage(StageType.TRAIN, RunStatus.PENDING),
                        Stage(StageType.TEST, RunStatus.PENDING),
                        Stage(StageType.DEPLOY, RunStatus.PENDING)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(5),
                    updatedAt = LocalDateTime.now().minusMinutes(5),
                ),
                Run(
                    runNumber = 7,
                    projectId = projectId,
                    status = RunStatus.FAILED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.COMPLETED),
                        Stage(StageType.TRAIN, RunStatus.COMPLETED),
                        Stage(StageType.TEST, RunStatus.FAILED),
                        Stage(StageType.DEPLOY, RunStatus.PENDING)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(10),
                    updatedAt = LocalDateTime.now().minusMinutes(10),
                ),
                Run(
                    runNumber = 6,
                    projectId = projectId,
                    status = RunStatus.FAILED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.COMPLETED),
                        Stage(StageType.TRAIN, RunStatus.COMPLETED),
                        Stage(StageType.TEST, RunStatus.FAILED),
                        Stage(StageType.DEPLOY, RunStatus.FAILED)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(15),
                    updatedAt = LocalDateTime.now().minusMinutes(15),
                ),
                Run(
                    runNumber = 5,
                    projectId = projectId,
                    status = RunStatus.COMPLETED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.COMPLETED),
                        Stage(StageType.TRAIN, RunStatus.COMPLETED),
                        Stage(StageType.TEST, RunStatus.COMPLETED),
                        Stage(StageType.DEPLOY, RunStatus.COMPLETED)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(20),
                    updatedAt = LocalDateTime.now().minusMinutes(20),
                ),
                Run(
                    runNumber = 4,
                    projectId = projectId,
                    status = RunStatus.RUNNING,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.PENDING),
                        Stage(StageType.TRAIN, RunStatus.PENDING),
                        Stage(StageType.TEST, RunStatus.RUNNING),
                        Stage(StageType.DEPLOY, RunStatus.PENDING)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(30),
                    updatedAt = LocalDateTime.now().minusMinutes(30),
                ),
                Run(
                    runNumber = 3,
                    projectId = projectId,
                    status = RunStatus.FAILED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.PENDING),
                        Stage(StageType.TRAIN, RunStatus.PENDING),
                        Stage(StageType.TEST, RunStatus.FAILED),
                        Stage(StageType.DEPLOY, RunStatus.FAILED)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(40),
                    updatedAt = LocalDateTime.now().minusMinutes(40),
                ),
                Run(
                    runNumber = 2,
                    projectId = projectId,
                    status = RunStatus.FAILED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.COMPLETED),
                        Stage(StageType.TRAIN, RunStatus.COMPLETED),
                        Stage(StageType.TEST, RunStatus.FAILED),
                        Stage(StageType.DEPLOY, RunStatus.FAILED)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(50),
                    updatedAt = LocalDateTime.now().minusMinutes(50)
                ),
                Run(
                    runNumber = 1,
                    projectId = projectId,
                    status = RunStatus.COMPLETED,
                    stages = listOf(
                        Stage(StageType.PREPARE, RunStatus.COMPLETED),
                        Stage(StageType.TRAIN, RunStatus.COMPLETED),
                        Stage(StageType.TEST, RunStatus.COMPLETED),
                        Stage(StageType.DEPLOY, RunStatus.COMPLETED)
                    ),
                    createdAt = LocalDateTime.now().minusMinutes(60),
                    updatedAt = LocalDateTime.now().minusMinutes(60)
                )
            )

            runRepository.saveAll(runs)
            println("Инициализация завершена")
        } else {
            println("Коллекция runs уже содержит данные")
        }
    }
}
