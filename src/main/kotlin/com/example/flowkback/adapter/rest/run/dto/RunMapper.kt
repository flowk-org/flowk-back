package com.example.flowkback.adapter.rest.run.dto

import com.example.flowkback.domain.run.Run
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class RunMapper {
    fun domainPageToDto(run: Run): RunDto {
        return run.let {
            RunDto(
                id = it.runNumber,
                date = it.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                status = it.status.name.lowercase(),
                stages = it.stages.map { it.status.name.lowercase() }
            )
        }
    }
}