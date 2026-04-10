package com.sharease.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.sharease.app.databinding.FragmentQrScanBinding

class QrScanFragment : Fragment() {
    
    private var _binding: FragmentQrScanBinding? = null
    private val binding get() = _binding!!

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(requireContext(), "Camera ready for QR scanning", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Camera permission required for QR scanning", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (hasCameraPermission()) {
            Toast.makeText(requireContext(), "Camera ready for QR scanning", Toast.LENGTH_SHORT).show()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        binding.tvScanResult.text = "Use QR Scanner to scan connection codes"
        binding.tvScanResult.visibility = View.VISIBLE
    }

    private fun hasCameraPermission(): Boolean {
        return requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
