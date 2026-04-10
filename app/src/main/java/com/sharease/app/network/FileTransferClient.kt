package com.sharease.app.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket

class FileTransferClient(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    sealed class TransferResult {
        data class Success(val message: String) : TransferResult()
        data class Error(val error: String) : TransferResult()
        data class Progress(val progress: Float, val currentFile: String) : TransferResult()
    }

    suspend fun sendText(ipAddress: String, port: Int, text: String): Flow<TransferResult> = flow {
        try {
            val socket = Socket(ipAddress, port)
            val outputStream = socket.getOutputStream()
            val dataOutputStream = DataOutputStream(outputStream)
            
            dataOutputStream.writeUTF("TEXT")
            dataOutputStream.writeUTF(text)
            dataOutputStream.flush()
            
            socket.close()
            emit(TransferResult.Success("Text sent successfully"))
        } catch (e: Exception) {
            emit(TransferResult.Error("Failed to send: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendFiles(ipAddress: String, port: Int, files: List<Pair<Uri, String>>): Flow<TransferResult> = flow {
        try {
            val socket = Socket(ipAddress, port)
            val outputStream = socket.getOutputStream()
            val dataOutputStream = DataOutputStream(outputStream)
            
            dataOutputStream.writeUTF("FILES")
            dataOutputStream.writeInt(files.size)
            dataOutputStream.flush()
            
            for ((index, filePair) in files.withIndex()) {
                val uri = filePair.first
                val fileName = filePair.second
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var totalRead = 0L
                    val fileSize = getFileSize(uri)
                    
                    dataOutputStream.writeUTF(fileName)
                    dataOutputStream.writeLong(fileSize)
                    dataOutputStream.writeUTF(getMimeType(uri) ?: "application/octet-stream")
                    dataOutputStream.flush()
                    
                    val bufferedOutputStream = BufferedOutputStream(outputStream)
                    
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        bufferedOutputStream.write(buffer, 0, read)
                        totalRead += read
                        
                        val progress = if (fileSize > 0) totalRead.toFloat() / fileSize else 0f
                        val currentFile = "${index + 1}/${files.size}: $fileName"
                        emit(TransferResult.Progress(progress, currentFile))
                    }
                    
                    bufferedOutputStream.flush()
                }
            }
            
            socket.close()
            emit(TransferResult.Success("Files sent successfully"))
        } catch (e: Exception) {
            emit(TransferResult.Error("Failed to send: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    fun cancel() {
        scope.cancel()
    }
}
