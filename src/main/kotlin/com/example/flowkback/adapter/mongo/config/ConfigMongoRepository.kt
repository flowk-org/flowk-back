package com.example.flowkback.adapter.mongo.config

import com.example.flowkback.domain.config.Config
import org.springframework.data.mongodb.repository.MongoRepository

interface ConfigMongoRepository : MongoRepository<Config, String> {
}