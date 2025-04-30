package com.example.flowkback.app.api

interface CreateContainerOutbound {
    /**
     * Создать контейнер
     *
     * @param image образ контейнер
     * @param containerName название контейнера
     * @param mounts точки монтирования
     * @return идентификатор контейнера
     */
    fun create(
        image: String,
        containerName: String,
        mounts: List<Mount> = listOf()
    ): String
}