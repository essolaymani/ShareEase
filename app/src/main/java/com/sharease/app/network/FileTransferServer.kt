package com.sharease.app.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class FileTransferServer(
    private val context: Context,
    private val port: Int = 8080
) {
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val receivedItems = mutableListOf<ReceivedFileData>()

    data class ReceivedFileData(
        val name: String,
        val size: Long,
        val path: String,
        val mimeType: String?
    )

    fun start(onStatusChange: (Boolean) -> Unit, onProgress: (Float, String) -> Unit, onFileReceived: (ReceivedFileData) -> Unit) {
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                withContext(Dispatchers.Main) {
                    onStatusChange(true)
                }
                
                while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                    try {
                        val clientSocket = serverSocket!!.accept()
                        launch { handleClient(clientSocket, onProgress, onFileReceived) }
                    } catch (e: Exception) {
                        if (!isClosed) e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onStatusChange(false)
                }
            }
        }
    }

    private suspend fun handleClient(
        socket: Socket,
        onProgress: (Float, String) -> Unit,
        onFileReceived: (ReceivedFileData) -> Unit
    ) {
        try {
            val inputStream = socket.getInputStream()
            val dataInputStream = DataInputStream(inputStream)
            
            val type = dataInputStream.readUTF()
            
            when (type) {
                "TEXT" -> {
                    val text = dataInputStream.readUTF()
                    val fileData = ReceivedFileData(
                        name = "Text Message",
                        size = text.length.toLong(),
                        path = text,
                        mimeType = "text/plain"
                    )
                    withContext(Dispatchers.Main) {
                        onFileReceived(fileData)
                    }
                }
                "FILES" -> {
                    val fileCount = dataInputStream.readInt()
                    val baseDir = context.getExternalFilesDir(null)
                    
                    for (i in 0 until fileCount) {
                        val fileName = dataInputStream.readUTF()
                        val fileSize = dataInputStream.readLong()
                        val mimeType = dataInputStream.readUTF()
                        
                        val outputFile = File(baseDir, fileName)
                        val outputStream = FileOutputStream(outputFile)
                        val bufferedOutputStream = BufferedOutputStream(outputStream)
                        
                        val buffer = ByteArray(8192)
                        var totalRead = 0L
                        var read: Int
                        
                        while (totalRead < fileSize) {
                            read = inputStream.read(buffer)
                            if (read == -1) break
                            bufferedOutputStream.write(buffer, 0, read)
                            totalRead += read
                            
                            val progress = totalRead.toFloat() / fileSize
                            val currentFile = "${i + 1}/$fileCount: $fileName"
                            withContext(Dispatchers.Main) {
                                onProgress(progress, currentFile)
                            }
                        }
                        
                        bufferedOutputStream.flush()
                        bufferedOutputStream.close()
                        
                        val fileData = ReceivedFileData(
                            name = fileName,
                            size = fileSize,
                            path = outputFile.absolutePath,
                            mimeType = mimeType
                        )
                        withContext(Dispatchers.Main) {
                            onFileReceived(fileData)
                        }
                    }
                }
            }
            
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        scope.cancel()
        serverSocket?.close()
        serverSocket = null
    }

    private fun ServerSocket.isClosed(): Boolean {
        return try {
            isClosed
        } catch (e: Exception) {
            true
        }
    }
}
