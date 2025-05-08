package com.example.flowkback.adapter.mongo.project

import com.example.flowkback.domain.project.Project
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : MongoRepository<Project, String> {
    fun findByName(name: String): Project?
}
