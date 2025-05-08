package com.example.flowkback.adapter.mongo.run

import com.example.flowkback.domain.run.Run
import com.example.flowkback.domain.run.RunStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RunRepository : MongoRepository<Run, String> {
    fun findAllByProjectId(projectId: String): List<Run>
    fun findAllByProjectIdAndStatus(projectId: String, status: RunStatus): List<Run>
}
