package com.example.flowkback.adapter.rest

import com.example.flowkback.app.impl.TrainModelUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
class TestController (private val train: TrainModelUseCase) {

    @GetMapping()
    fun get() {
        train.execute(
            trainScript = File("data/train.py"),
            modelName = "model"
        )
    }

}