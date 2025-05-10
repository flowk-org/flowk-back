package com.example.flowkback.app.api.docker

interface CreateContainerOutbound {
    /**
     * Создать контейнер
     *
     * @param image образ контейнер
     * @param containerName название контейнера
     * @param mounts точки монтирования
     * @param ports проброс портов
     * @param network сеть контейнера
     * @return идентификатор контейнера
     * @throws
     */
    fun create(
        image: String,
        containerName: String,
        mounts: List<Mount> = listOf(),
        ports: List<PortForwarding> = listOf(),
        network: String = "bridge"
    ): String
}