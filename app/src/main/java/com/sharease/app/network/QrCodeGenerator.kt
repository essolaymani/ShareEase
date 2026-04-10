package com.sharease.app.network

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    
    fun generateQRCode(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createConnectionString(ipAddress: String, port: Int): String {
        return "sharease://$ipAddress:$port"
    }

    fun parseConnectionString(connectionString: String): Pair<String, Int>? {
        return try {
            val cleanString = connectionString.replace("sharease://", "")
            val parts = cleanString.split(":")
            if (parts.size == 2) {
                Pair(parts[0], parts[1].toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
