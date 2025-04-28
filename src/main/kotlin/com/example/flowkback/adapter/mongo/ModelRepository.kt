package com.example.flowkback.adapter.mongo

import com.example.flowkback.domain.ModelArtifact
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ModelRepository : MongoRepository<ModelArtifact, String>