package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import java.io.File
import java.io.IOException
import kotlin.math.pow

class ConverterService(private val context: Context) {
    fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageFilePath(imageUri: Uri): String {
        val context = context.applicationContext
        val filePath: String
        val cursor = context.contentResolver.query(imageUri, null, null, null, null)
        if (cursor == null) {
            filePath = imageUri.path ?: imageUri.toString()
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }


    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    fun rotateBitmapIfRequired(bitmap: Bitmap, imageUri: Uri): Bitmap? {
        val exif = ExifInterface(getImageFilePath(imageUri))

        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }


    fun getBitmapFormat(format: String): Bitmap.CompressFormat {
        return when (format) {
            "png" -> Bitmap.CompressFormat.PNG
            "jpeg" -> Bitmap.CompressFormat.JPEG
            "webp" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.PNG
        }
    }

    fun getImgName(imageUri: Uri?): String {
        if (imageUri != null) {
            val img = imageUri.toString()
            val parts = img.split("/")
            val fileNameWithExtension = parts.last()
            val fileName = fileNameWithExtension.substringBeforeLast(".")
            if (fileName != "null") {
                return "ai-converted-$fileName"
            }
        }
        return "ai-converted-image-${System.currentTimeMillis()}"
    }

    fun changeQuality(n: Int): Int {
        val numDigits = n.toString().length
        return n / 10.0.pow((numDigits - 1).toDouble()).toInt()
    }

    fun getOutputPath(imageName: String, format: String): String {
        return "${getOutputDir().absolutePath}/${imageName}.${format}"
    }

    private fun getOutputDir(): File {
        val mediaDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "AI-Image-Converter"
        )
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        return if (mediaDir.exists())
            mediaDir else context.filesDir
    }

    private fun getPageSize(name: String): Rectangle {
        when (name) {
            "LETTER" -> return PageSize.LETTER
            "NOTE" -> return PageSize.NOTE
            "LEGAL" -> return PageSize.LEGAL
            "TABLOID" -> return PageSize.TABLOID
            "EXECUTIVE" -> return PageSize.EXECUTIVE
            "POSTCARD" -> return PageSize.POSTCARD
            "A0" -> return PageSize.A0
            "A1" -> return PageSize.A1
            "A2" -> return PageSize.A2
            "A3" -> return PageSize.A3
            "A4" -> return PageSize.A4
            "A5" -> return PageSize.A5
            "A6" -> return PageSize.A6
            "A7" -> return PageSize.A7
            "A8" -> return PageSize.A8
            "A9" -> return PageSize.A9
            "A10" -> return PageSize.A10
            "B0" -> return PageSize.B0
            "B1" -> return PageSize.B1
            "B2" -> return PageSize.B2
            "B3" -> return PageSize.B3
            "B4" -> return PageSize.B4
            "B5" -> return PageSize.B5
            "B6" -> return PageSize.B6
            "B7" -> return PageSize.B7
            "B8" -> return PageSize.B8
            "B9" -> return PageSize.B9
            "B10" -> return PageSize.B10
            "ARCH_E" -> return PageSize.ARCH_E
            "ARCH_D" -> return PageSize.ARCH_D
            "ARCH_C" -> return PageSize.ARCH_C
            "ARCH_B" -> return PageSize.ARCH_B
            "ARCH_A" -> return PageSize.ARCH_A
            "FLSA" -> return PageSize.FLSA
            "FLSE" -> return PageSize.FLSE
            "HALFLETTER" -> return PageSize.HALFLETTER
            "_11X17" -> return PageSize._11X17
            "ID_1" -> return PageSize.ID_1
            "ID_2" -> return PageSize.ID_2
            "ID_3" -> return PageSize.ID_3
            "LEDGER" -> return PageSize.LEDGER
            "CROWN_QUARTO" -> return PageSize.CROWN_QUARTO
            "LARGE_CROWN_QUARTO" -> return PageSize.LARGE_CROWN_QUARTO
            "DEMY_QUARTO" -> return PageSize.DEMY_QUARTO
            "ROYAL_QUARTO" -> return PageSize.ROYAL_QUARTO
            "CROWN_OCTAVO" -> return PageSize.CROWN_OCTAVO
            "LARGE_CROWN_OCTAVO" -> return PageSize.LARGE_CROWN_OCTAVO
            "DEMY_OCTAVO" -> return PageSize.DEMY_OCTAVO
            "ROYAL_OCTAVO" -> return PageSize.ROYAL_OCTAVO
            "SMALL_PAPERBACK" -> return PageSize.SMALL_PAPERBACK
            "PENGUIN_SMALL_PAPERBACK" -> return PageSize.PENGUIN_SMALL_PAPERBACK
            "PENGUIN_LARGE_PAPERBACK" -> return PageSize.PENGUIN_LARGE_PAPERBACK
            else -> return PageSize.A4
        }
    }

    fun convertImgToPdf(document: Document, imageUri: Uri, pdfPageSize: String, isSingle: Boolean) {

        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val image = Image.getInstance(imageBytes)

        if (pdfPageSize == "Free Size") {
            document.pageSize = Rectangle(image.scaledWidth, image.scaledHeight)
        } else {
            document.pageSize = getPageSize(pdfPageSize)

        }
        val rotationAngle = image.rotation
        image.rotation = rotationAngle
        image.scaleToFit(document.pageSize.width, document.pageSize.height)
        val imageWidth = image.scaledWidth
        val imageHeight = image.scaledHeight

        val x = (document.pageSize.width - imageWidth) / 2
        val y = (document.pageSize.height - imageHeight) / 2
        image.setAbsolutePosition(x, y)
        document.add(image)
        if (!isSingle) {
            document.newPage()
        }

        inputStream?.close()
    }


}