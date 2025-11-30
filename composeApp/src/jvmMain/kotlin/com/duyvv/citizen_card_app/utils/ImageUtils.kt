package com.duyvv.citizen_card_app.utils

import com.duyvv.citizen_card_app.domain.model.ImageFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun pickImageFromSystem(): ImageFile? {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Chọn Ảnh"

    // Chỉ lọc các file ảnh
    val filter = FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "JPEG")
    fileChooser.fileFilter = filter

    val result = fileChooser.showOpenDialog(null)

    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile: File = fileChooser.selectedFile
        return try {
            val format = selectedFile.extension.ifEmpty { "JPG" }

            val bytes = convertImageToByteArray(selectedFile.absolutePath, format)

            println("Selected file: ${selectedFile.absolutePath}")
            println("Optimized size: ${bytes.size} bytes")

            ImageFile(bytes, selectedFile.name)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    return null
}

fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    // Handle case where image type is 0 (CUSTOM) to avoid errors
    val type = if (originalImage.type == 0) BufferedImage.TYPE_INT_ARGB else originalImage.type

    val resizedImage = BufferedImage(targetWidth, targetHeight, type)
    val g2d = resizedImage.createGraphics()
    g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
    g2d.dispose()
    return resizedImage
}

@Throws(IOException::class)
fun convertImageToByteArray(imagePath: String, format: String): ByteArray {
    val quality = 0.25f // Set the desired compression quality (0.0 to 1.0)
    val maxWidth = 600 // Maximum width to resize image (if needed)
    val maxHeight = 400 // Maximum height to resize image (if needed)

    var image = ImageIO.read(File(imagePath))
        ?: throw IOException("Invalid image file: $imagePath")

    if (image.width > maxWidth || image.height > maxHeight) {
        image = resizeImage(image, maxWidth, maxHeight)
    }

    val baos = ByteArrayOutputStream()
    val upperFormat = format.uppercase()

    if (upperFormat == "JPG" || upperFormat == "JPEG" || upperFormat == "PNG") {
        val writers = ImageIO.getImageWritersByFormatName(upperFormat)

        if (writers.hasNext()) {
            val writer = writers.next()
            val param = writer.defaultWriteParam

            if (param.canWriteCompressed()) {
                param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                param.compressionQuality = quality
            }

            val ios = ImageIO.createImageOutputStream(baos)
            writer.output = ios
            writer.write(null, IIOImage(image, null, null), param)
            writer.dispose()
            ios.close()
        } else {
            ImageIO.write(image, upperFormat, baos)
        }
    } else {
        ImageIO.write(image, upperFormat, baos)
    }

    return baos.toByteArray()
}