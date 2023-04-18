package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai


import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.MotionButton
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityImageConverterBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services.ConverterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class ImageConverter : AppCompatActivity() {

    private lateinit var binding: ActivityImageConverterBinding
    private var viewId: Int? = null
    private var imageUri: Uri? = null

    private lateinit var imageName: String

    private var quality = 100

    private var pdfCompression = 0
    private var pdfPageSize = "Free Size"

    private val motionButtonIds = intArrayOf(
        R.id.png, R.id.jpg, R.id.jpeg, R.id.webp,
        R.id.pdf
    )

    private val thread = CoroutineScope(Dispatchers.IO)

    private val converter = ConverterService(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        setUpAd()

        imageName = converter.getImgName(imageUri)

        setImage()

        binding.convertImageBtn.setOnClickListener() {
            disableButton()
            thread.launch {
                convertImage()
            }
        }

        binding.compressionLevel.addOnChangeListener { _, value, _ ->
            val intValue = value.toInt()
            quality = 101 - intValue
            if (intValue >= 10) {
                pdfCompression = converter.changeQuality(intValue)
            }
        }

        binding.spinnerPageSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pdfPageSize = binding.spinnerPageSize.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing

                }
            }

    }

    private fun setUpAd() {
        try {

            val adRequest = AdRequest.Builder().build()
            val adRequestTwo = AdRequest.Builder().build()

            binding.apply {
                converterAdView.loadAd(adRequest)
                converterAdViewTwo.loadAd(adRequestTwo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun disableButton() {

        runOnUiThread {
            changeMotionButton(false)
            binding.convertImageBtn.apply {
                isEnabled = false
                text = "Converting..."
            }
            binding.progressBar.visibility = View.VISIBLE
        }

    }

    @SuppressLint("SetTextI18n")
    private fun enableButton() {
        runOnUiThread {
            changeMotionButton(true)
            binding.convertImageBtn.apply {
                isEnabled = true
                text = "Convert Image"
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun changeMotionButton(isEnabled: Boolean) {
        for (id in motionButtonIds) {
            val motionButton = findViewById<MotionButton>(id)
            motionButton.isEnabled = isEnabled
        }
    }

    private fun setImage() {
        val imageUrl = intent.extras?.getString("imageUrl")
        if (imageUrl != null) {
            imageUri = Uri.parse(imageUrl)
        }
        binding.imageView3.setImageURI(imageUri)
    }

    fun getConverterType(view: View) {
        runOnUiThread {
            for (id in motionButtonIds) {
                val motionButton = findViewById<MotionButton>(id)
                if (id == view.id) {
                    viewId = id
                    motionButton.setBackgroundResource(R.drawable.set_button_background)
                    enableButton()
                } else {
                    motionButton.setBackgroundResource(R.drawable.button_background)
                }
            }
        }
    }

    private fun convertImage() {
        when (viewId) {
            motionButtonIds[0] -> {
                convertImageToFormat("png")
            }
            motionButtonIds[1] -> {
                convertImageToFormat("jpg")
            }
            motionButtonIds[2] -> {
                convertImageToFormat("jpeg")
            }
            motionButtonIds[3] -> {
                convertImageToFormat("webp")
            }
            motionButtonIds[4] -> {
                convertImageToFormat("pdf")
            }

            else -> {
                Toast.makeText(this, "Please select a format", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun convertImageToFormat(format: String) {
        var type = "image"
        val newImageUri: Uri?
        val path = converter.getOutputPath(imageName, format)
        val outputStream = FileOutputStream(path)
        val bitmap = converter.getBitmapFromUri(imageUri!!)

        if (format == "pdf") {
            type = "pdf"
            convertToPdf(outputStream)
        } else {
            val rotatedBitmap = converter.rotateBitmapIfRequired(bitmap!!, imageUri!!)
            rotatedBitmap?.compress(converter.getBitmapFormat(format), quality, outputStream)
        }

        outputStream.flush()
        outputStream.close()

        newImageUri = if (format == "jpg") {
            val outputFile = File(path)
            val newOutputPath = path.replace(".jpeg", ".jpg")
            outputFile.renameTo(File(newOutputPath))
            Uri.fromFile(File(newOutputPath))
        } else {
            Uri.fromFile(File(path))
        }

        if (newImageUri != null) {
            enableButton()
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Photo converted and saved to Documents/AI-Image-Converter folder ",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            converter.gotoResult(newImageUri, type)
        }
    }


    private fun convertToPdf(outputStream: FileOutputStream) {
        try {
            val document = Document()

            val pdfWriter = PdfWriter.getInstance(document, outputStream)
            pdfWriter.compressionLevel = pdfCompression

            document.open()

            converter.convertImgToPdf(document, imageUri!!, pdfPageSize, true)

            document.close()
            pdfWriter.close()
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Unable to convert to PDF - ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}