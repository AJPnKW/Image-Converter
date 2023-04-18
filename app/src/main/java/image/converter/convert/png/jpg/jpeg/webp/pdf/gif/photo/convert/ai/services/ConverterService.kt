package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services

import android.content.Context
import android.content.Intent
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
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.FinalResult
import java.io.File
import java.io.IOException
import java.io.InputStream
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

    fun convertImgToPdf(document: Document, imageUri: Uri, pdfPageSize: String, isSingle: Boolean) {

        var inputStream: InputStream? = null

        val image: Image = if (isSingle) {
            Image.getInstance(imageUri.toString())
        } else {
            inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            Image.getInstance(imageBytes)
        }

        if (pdfPageSize == "Free Size") {
            document.pageSize = Rectangle(image.scaledWidth, image.scaledHeight)
        } else {
            document.pageSize = PageSize.getRectangle(pdfPageSize)
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

    fun gotoResult(fileUri: Uri, type: String) {
        val i = Intent(context, FinalResult::class.java)
        i.putExtra("uri", fileUri.toString())
        i.putExtra("type", type)
        context.startActivity(i)
    }

}