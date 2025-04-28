package com.example.flowkback.adapter.mongo

import com.example.flowkback.domain.Metric
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricRepository : MongoRepository<Metric, String>