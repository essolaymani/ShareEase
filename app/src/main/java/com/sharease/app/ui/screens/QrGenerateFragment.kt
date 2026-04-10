package com.sharease.app.ui.screens

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sharease.app.databinding.FragmentQrGenerateBinding
import com.sharease.app.network.NetworkUtils
import com.sharease.app.network.QrCodeGenerator

class QrGenerateFragment : Fragment() {
    
    private var _binding: FragmentQrGenerateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrGenerateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateQRCode()
        
        binding.btnRefreshQr.setOnClickListener {
            generateQRCode()
        }
    }

    private fun generateQRCode() {
        val ip = NetworkUtils.getIPAddress(requireContext())
        if (ip != null) {
            val connectionString = QrCodeGenerator.createConnectionString(ip, 8080)
            binding.tvQrInfo.text = connectionString
            
            val bitmap = QrCodeGenerator.generateQRCode(connectionString, 512)
            binding.ivQrCode.setImageBitmap(bitmap)
        } else {
            binding.tvQrInfo.text = "Not connected to WiFi"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
