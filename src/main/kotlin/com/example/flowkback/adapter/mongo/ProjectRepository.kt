package com.example.flowkback.adapter.mongo

import com.example.flowkback.domain.Project
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : MongoRepository<Project, String>
