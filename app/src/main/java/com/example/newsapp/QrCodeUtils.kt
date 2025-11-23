package com.example.newsapp

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtils {
    fun generateQrCode(content: String, width: Int = 512, height: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            // Створюємо матрицю бітів (де чорне, де біле)
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)

            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)

            // Проходимо по матриці і зафарбовуємо пікселі
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            // Створюємо Android Bitmap
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}