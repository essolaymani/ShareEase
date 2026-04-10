package com.sharease.app.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharease.app.data.model.ReceivedItem
import com.sharease.app.databinding.FragmentReceiveBinding
import com.sharease.app.network.FileTransferServer
import com.sharease.app.ui.components.ReceivedAdapter
import kotlinx.coroutines.*

class ReceiveFragment : Fragment() {
    
    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!
    
    private val receivedItems = mutableListOf<ReceivedItem>()
    private lateinit var receivedAdapter: ReceivedAdapter
    private var server: FileTransferServer? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        receivedAdapter = ReceivedAdapter(receivedItems)
        
        binding.rvReceived.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receivedAdapter
        }
        
        binding.btnStartReceive.setOnClickListener {
            if (binding.btnStartReceive.text == "Start Server") {
                startServer()
            } else {
                stopServer()
            }
        }
        
        updateEmptyState()
    }

    private fun updateEmptyState() {
        binding.tvNoReceived.visibility = if (receivedItems.isEmpty()) View.VISIBLE else View.GONE
        binding.rvReceived.visibility = if (receivedItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startServer() {
        server = FileTransferServer(requireContext())
        binding.btnStartReceive.text = "Stop Server"
        binding.tvReceiveStatus.text = "Waiting for files..."
        
        serverJob = scope.launch {
            server?.start(
                onStatusChange = { running ->
                    if (!running) {
                        binding.btnStartReceive.text = "Start Server"
                        binding.tvReceiveStatus.text = "Server stopped"
                    }
                },
                onProgress = { progress, currentFile ->
                    binding.progressReceive.progress = (progress * 100).toInt()
                    binding.tvReceiveProgress.text = "${(progress * 100).toInt()}%"
                    binding.tvReceiveSpeed.text = currentFile
                },
                onFileReceived = { fileData ->
                    val item = if (fileData.mimeType == "text/plain" && fileData.size < 10000) {
                        ReceivedItem(
                            name = fileData.name,
                            size = fileData.size,
                            type = ReceivedItem.ItemType.TEXT,
                            path = fileData.path
                        )
                    } else {
                        ReceivedItem(
                            name = fileData.name,
                            size = fileData.size,
                            type = ReceivedItem.ItemType.FILE,
                            path = fileData.path
                        )
                    }
                    receivedItems.add(0, item)
                    receivedAdapter.notifyDataSetChanged()
                    updateEmptyState()
                    binding.progressReceive.progress = 0
                    binding.tvReceiveProgress.text = "100%"
                    binding.tvReceiveSpeed.text = ""
                    Toast.makeText(requireContext(), "Received: ${fileData.name}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun stopServer() {
        serverJob?.cancel()
        server?.stop()
        server = null
        binding.btnStartReceive.text = "Start Server"
        binding.tvReceiveStatus.text = "Server stopped"
        binding.progressReceive.progress = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopServer()
        scope.cancel()
        _binding = null
    }
}
