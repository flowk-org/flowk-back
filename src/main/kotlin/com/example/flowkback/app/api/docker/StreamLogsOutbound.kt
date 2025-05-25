package com.example.flowkback.app.api.docker

import kotlinx.coroutines.flow.Flow

interface StreamLogsOutbound {
    /**
     * Получение логов контейнера в потоковом режиме
     *
     * @param containerId идентификатор контейнера
     * @return поток логов
     * @throws
     */
    fun stream(containerId: String): Flow<String>
}