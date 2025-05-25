package com.example.flowkback.app.api.docker

interface StartContainerOutbound {
    /**
     * Запустить контейнер
     *
     * @param containerId идентификатор контейнера
     * @return сообщение о состоянии контейнера
     * @throws
     */
    fun start(containerId: String): String
}
