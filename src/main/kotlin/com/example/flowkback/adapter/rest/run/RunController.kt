package com.example.flowkback.adapter.rest.run

import org.springframework.web.bind.annotation.*

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/runs")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000"])
class RunController {

    @GetMapping
    fun getRuns(): List<RunDto> {
        // Заглушка с историческими данными из JavaScript
        return listOf(
            RunDto(
                id = 4,
                date = "Feb 15, 21:20",
                status = "Running",
                stages = listOf("blank", "blank", "running", "pending"),
                replayOfRun = ReplayOfRunDto(id = 2, stage = 3)
            ),
            RunDto(
                id = 3,
                date = "Feb 15, 21:20",
                status = "Failed",
                stages = listOf("blank", "blank", "failed", "failed"),
                replayOfRun = ReplayOfRunDto(id = 2, stage = 3)
            ),
            RunDto(
                id = 2,
                date = "Feb 15, 21:20",
                status = "Failed",
                stages = listOf("completed", "completed", "failed", "failed"),
                replayedFromStages = listOf(3)
            ),
            RunDto(
                id = 1,
                date = "Feb 15, 21:18",
                status = "Completed",
                stages = listOf("completed", "completed", "completed", "completed")
            )
        )
    }
}


//@RestController
//@RequestMapping("/runs")
//class RunController(
//    private val runRepository: RunRepository,
//    private val projectRepository: ProjectRepository
//) {
//
//    @PostMapping
//    fun startRun(@RequestBody request: StartRunRequest): ResponseEntity<Run> {
//        val project = projectRepository.findByName(request.projectName)
//            ?: return ResponseEntity.badRequest().build()
//
//        val run = Run(
//            projectId = project.id!!,
//            configId = project.configId,
//            status = RunStatus.PENDING,
//            stages = listOf(),
//        )
//
//        val savedRun = runRepository.save(run)
//        return ResponseEntity.ok(savedRun)
//    }
//
//    @GetMapping("/project/{projectId}")
//    fun getRunsForProject(@PathVariable projectId: String): ResponseEntity<List<Run>> {
//        val runs = runRepository.findAllByProjectId(projectId)
//        return ResponseEntity.ok(runs)
//    }
//
//    @GetMapping("/{runId}")
//    fun getRun(@PathVariable runId: String): ResponseEntity<Run> {
//        val run = runRepository.findById(runId)
//        return if (run.isPresent) ResponseEntity.ok(run.get())
//        else ResponseEntity.notFound().build()
//    }
//}
