package com.sharease.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharease.app.data.model.FileItem
import com.sharease.app.databinding.FragmentSendBinding
import com.sharease.app.network.FileTransferClient
import com.sharease.app.network.FileTransferServer
import com.sharease.app.network.NetworkUtils
import com.sharease.app.network.QrCodeGenerator
import com.sharease.app.ui.components.FileAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.net.ServerSocket

class SendFragment : Fragment() {
    
    private var _binding: FragmentSendBinding? = null
    private val binding get() = _binding!!
    
    private val files = mutableListOf<FileItem>()
    private lateinit var fileAdapter: FileAdapter
    private var server: FileTransferServer? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val client = FileTransferClient(requireContext())
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        addFile(uri)
                    }
                } else {
                    data.data?.let { uri ->
                        addFile(uri)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        updateNetworkInfo()
    }

    private fun setupUI() {
        fileAdapter = FileAdapter(files) { position ->
            files.removeAt(position)
            fileAdapter.notifyDataSetChanged()
            updateEmptyState()
        }
        
        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = fileAdapter
        }
        
        binding.btnSelectFiles.setOnClickListener {
            selectFiles()
        }
        
        binding.btnStartServer.setOnClickListener {
            startServer()
        }
        
        binding.btnStopServer.setOnClickListener {
            stopServer()
        }
        
        binding.btnShare.setOnClickListener {
            shareContent()
        }
        
        updateEmptyState()
    }

    private fun updateNetworkInfo() {
        val ip = NetworkUtils.getIPAddress(requireContext())
        binding.tvIpAddress.text = ip ?: "Not connected"
        binding.tvPort.text = "8080"
    }

    private fun selectFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        filePickerLauncher.launch(intent)
    }

    private fun addFile(uri: android.net.Uri) {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                
                val name = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                val mimeType = requireContext().contentResolver.getType(uri)
                
                val fileItem = FileItem(uri, name, size, mimeType)
                files.add(fileItem)
                fileAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    private fun updateEmptyState() {
        binding.tvNoFiles.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        binding.rvFiles.visibility = if (files.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startServer() {
        server = FileTransferServer(requireContext())
        binding.btnStartServer.visibility = View.GONE
        binding.btnStopServer.visibility = View.VISIBLE
        
        serverJob = scope.launch {
            server?.start(
                onStatusChange = { running ->
                    if (!running) {
                        binding.btnStartServer.visibility = View.VISIBLE
                        binding.btnStopServer.visibility = View.GONE
                    }
                },
                onProgress = { progress, currentFile ->
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Receiving: $currentFile"
                },
                onFileReceived = { fileData ->
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Received: ${fileData.name}"
                    Toast.makeText(requireContext(), "Received: ${fileData.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun stopServer() {
        serverJob?.cancel()
        server?.stop()
        server = null
        binding.btnStartServer.visibility = View.VISIBLE
        binding.btnStopServer.visibility = View.GONE
        Toast.makeText(requireContext(), "Server stopped", Toast.LENGTH_SHORT).show()
    }

    private fun shareContent() {
        val ip = binding.tvIpAddress.text.toString()
        if (ip == "Not connected") {
            Toast.makeText(requireContext(), "Not connected to WiFi", Toast.LENGTH_SHORT).show()
            return
        }
        
        val hasText = binding.etText.text.toString().isNotBlank()
        val hasFiles = files.isNotEmpty()
        
        if (!hasText && !hasFiles) {
            Toast.makeText(requireContext(), "Add text or files to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        scope.launch {
            binding.btnShare.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.visibility = View.VISIBLE
            binding.tvStatus.text = "Sharing..."
            
            val port = 8080
            
            if (hasText) {
                client.sendText(ip, port, binding.etText.text.toString()).collect { result ->
                    when (result) {
                        is FileTransferClient.TransferResult.Success -> {
                            binding.tvStatus.text = result.message
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        is FileTransferClient.TransferResult.Error -> {
                            binding.tvStatus.text = result.error
                            Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                        }
                        is FileTransferClient.TransferResult.Progress -> {
                            binding.tvStatus.text = "Sending text..."
                        }
                    }
                }
            }
            
            if (hasFiles) {
                val fileList = files.map { Pair(it.uri, it.name) }
                client.sendFiles(ip, port, fileList).collect { result ->
                    when (result) {
                        is FileTransferClient.TransferResult.Success -> {
                            binding.tvStatus.text = result.message
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        is FileTransferClient.TransferResult.Error -> {
                            binding.tvStatus.text = result.error
                            Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                        }
                        is FileTransferClient.TransferResult.Progress -> {
                            val progressPercent = (result.progress * 100).toInt()
                            binding.tvStatus.text = "${result.currentFile} - $progressPercent%"
                        }
                    }
                }
            }
            
            binding.btnShare.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}
