package com.example.flowkback.domain

data class MlciConfig(
    val prepare: PrepareStage,
    val train: TrainStage,
    val test: TestStage,
    val deploy: DeployStage
)

data class PrepareStage(
    val type: String = "clickhouse",
    val sqlPath: String
)

data class TrainStage(
    val script: String,
    val requirements: String,
    val output: String
)

data class TestStage(
    val script: String
)

data class DeployStage(
    val script: String
)
