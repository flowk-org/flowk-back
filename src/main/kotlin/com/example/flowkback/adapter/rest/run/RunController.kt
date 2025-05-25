package com.example.flowkback.adapter.rest.run

import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.adapter.rest.run.dto.RunDto
import com.example.flowkback.adapter.rest.run.dto.RunMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter


@RestController
@RequestMapping("/api/runs")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000"])
class RunController(
    private val runRepository: RunRepository,
    private val runMapper: RunMapper
) {

    @GetMapping("/count")
    fun getRunsCount(): Long {
        return runRepository.count()
    }

    @GetMapping
    fun getRunsPages(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") size: Int
    ): Page<RunDto> {
        return runRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, size))
            .map { runMapper.domainPageToDto(it) }
    }
}