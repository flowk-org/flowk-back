package com.example.flowkback.app.api.docker

interface RemoveContainerOutbound {
    /**
     * Удалить контейнер
     *
     * @param containerId идентификатор контейнера
     * @return сообщение о состоянии контейнера
     * @throws
     */
    fun remove(containerId: String): String
}