package com.example.flowkback.app.api

import java.io.File


interface BuildImageOutbound {
    /**
     * Собрать образ
     *
     * @param dockerfileDir путь до Dockerfile
     * @param imageName название образа
     *
     * @return идентификатор образа
     */
    fun build(dockerfileDir: File, imageName: String): String
}
