package com.example.flowkback.adapter.mongo.model

import com.example.flowkback.domain.model.ModelArtifact
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ModelRepository : MongoRepository<ModelArtifact, String> {
    fun findAllByProjectId(projectId: String): List<ModelArtifact>
    fun findAllByRunId(runId: String): List<ModelArtifact>
}
