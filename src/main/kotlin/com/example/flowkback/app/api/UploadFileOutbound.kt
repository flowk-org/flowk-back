package com.example.flowkback.app.api

import java.io.InputStream

interface UploadFileOutbound {
    fun upload(
        inputStream: InputStream,
        fileName: String,
        contentType: String = "application/octet-stream",
        bucketName: String
    ): String
}