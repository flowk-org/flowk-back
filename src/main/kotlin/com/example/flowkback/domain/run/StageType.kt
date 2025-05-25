package com.example.flowkback.domain.run

enum class StageType(val index: Int) {
    PREPARE(0),
    TRAIN(1),
    TEST(2),
    DEPLOY(3),
}