package com.example.flowkback.app.impl

import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class GenerateDockerfileDelegate {

    companion object {
        private const val DOCKERFILE_NAME = "Dockerfile"
        private const val DEFAULT_WORKDIR = "/app"
    }

    fun generate(
        pythonVersion: String,
        workDir: String = DEFAULT_WORKDIR,
        outputDirectory: String = ".",
        requirementsFile: String = "requirements.txt",
        trainScriptFile: String = "./repos/flowk-test/train.py",
        entrypoint: String = "python",
        command: String = "train.py"
    ): File {
        validatePythonVersion(pythonVersion)

        val dockerfileContent = """
            # Auto-generated Dockerfile
            FROM python:${pythonVersion}-slim

            RUN mkdir -p $workDir

            WORKDIR $workDir

            COPY $requirementsFile $workDir/$requirementsFile
            RUN pip install --no-cache-dir -r $requirementsFile

            COPY $trainScriptFile $workDir/$command

            ENTRYPOINT [ "$entrypoint" ]
            CMD [ "$command" ]
        """.trimIndent()

        return createDockerfile(outputDirectory, dockerfileContent)
    }

    fun generate(
        clickhouseImage: String = "yandex/clickhouse-client:latest",
        dir: String,
        workDir: String = DEFAULT_WORKDIR,
        outputDirectory: String = ".",
        migrationsDir: String = "./migrations",
        scriptFile: String
    ): File {
        validateClickhouseImage(clickhouseImage)

        val dockerfileContent = """
        FROM $clickhouseImage

        RUN apt-get update && apt-get install -y bash clickhouse-client

        RUN mkdir -p $workDir

        WORKDIR $workDir

        COPY $dir $workDir
        COPY $scriptFile $workDir/migrate.sh

        RUN chmod +x $workDir/migrate.sh

        ENTRYPOINT [ "./migrate.sh" ]
        CMD [ "/app/migrations" ]
    """.trimIndent()

        return createDockerfile(outputDirectory, dockerfileContent)
    }

    fun validateClickhouseImage(image: String) {
        val regex = Regex("^[a-zA-Z0-9][a-zA-Z0-9_.-]*(/[a-zA-Z0-9][a-zA-Z0-9_.-]*)*(:[a-zA-Z0-9_.-]+)?$")
        if (!image.matches(regex)) {
            throw IllegalArgumentException("Invalid ClickHouse image format: $image. Expected format: repository:tag (e.g., clickhouse/clickhouse-server:latest)")
        }
    }

    private fun validatePythonVersion(version: String) {
        require(version.matches(Regex("""^\d+\.\d+(\.\d+)?$"""))) {
            "Invalid Python version format. Expected format: X.Y or X.Y.Z"
        }
    }

    private fun createDockerfile(directory: String, content: String): File {
        val outputPath = Paths.get(directory).toAbsolutePath()
        ensureDirectoryExists(outputPath)

        val dockerfilePath = outputPath.resolve(DOCKERFILE_NAME)
        Files.write(dockerfilePath, content.toByteArray())

        return dockerfilePath.toFile().also {
            require(it.exists()) { "Failed to create Dockerfile" }
        }
    }

    private fun ensureDirectoryExists(path: Path) {
        if (!Files.exists(path)) {
            Files.createDirectories(path)
        }
        require(Files.isDirectory(path)) { "Path $path is not a directory" }
        require(Files.isWritable(path)) { "No write permissions for directory $path" }
    }
}