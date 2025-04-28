package com.example.flowkback.adapter.mongo

import com.example.flowkback.domain.Feature
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FeatureRepository : MongoRepository<Feature, String>