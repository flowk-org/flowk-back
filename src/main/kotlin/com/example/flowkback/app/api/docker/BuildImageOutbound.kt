package com.example.flowkback.app.api.docker

import java.io.File


interface BuildImageOutbound {
    /**
     * Собрать образ
     *
     * @param dockerfile путь до Dockerfile
     * @param imageName название образа
     * @return идентификатор образа
     */
    fun build(dockerfile: File, imageName: String): String
}
