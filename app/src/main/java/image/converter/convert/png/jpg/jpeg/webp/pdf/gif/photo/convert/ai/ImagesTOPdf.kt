package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter.ImageAdapter
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityImagesTopdfBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services.ConverterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ImagesTOPdf : AppCompatActivity() {

    private lateinit var binding: ActivityImagesTopdfBinding

    private var pdfPageSize = "Free Size"
    private val converter = ConverterService(this)

    private val thread = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagesTopdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        setUpAd()

        val imageUris = getImages()
        if (imageUris != null) {
            setImages(imageUris)

            binding.pdfConvertBtn.setOnClickListener() {
                disableButton()
                thread.launch {
                    convertToPdf(imageUris)
                }
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
                firstAd.loadAd(adRequest)
                secondAd.loadAd(adRequestTwo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getImages(): List<Uri>? {
        val imageUrisJson = intent.extras?.getString("imageUrisJson")
        return try {
            run {
                val imageUrisStrings: List<String> =
                    Gson().fromJson(imageUrisJson, object : TypeToken<List<String>>() {}.type)
                val uris = imageUrisStrings.map { Uri.parse(it) }
                uris
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun setImages(uris: List<Uri>) {
        val imgLayout = GridLayoutManager(this, 3)
        val imageAdapter = ImageAdapter()

        imgLayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1 // Return 1 for each item, assuming each item occupies 1 span
            }
        }

        binding.imagesRecyclerView.apply {
            layoutManager = imgLayout
            adapter = imageAdapter
        }
        imageAdapter.differ.submitList(uris)
    }

    @SuppressLint("SetTextI18n")
    private fun disableButton() {
        runOnUiThread {
            binding.pdfConvertBtn.apply {
                isEnabled = false
                text = "Converting..."
            }
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun enableButton() {
        runOnUiThread {
            binding.pdfConvertBtn.apply {
                isEnabled = true
                text = "Convert to PDF"
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun convertToPdf(imageUris: List<Uri>) {
        try {
            val imgName = converter.getImgName(null)
            val path = converter.getOutputPath(imgName, "pdf")
            val outputStream = FileOutputStream(path)

            val document = Document()
            val pdfWriter = PdfWriter.getInstance(document, outputStream)
            document.open()

            for (imageUri in imageUris) {
                converter.convertImgToPdf(document, imageUri, pdfPageSize, false)
            }

            document.close()
            pdfWriter.close()

            outputStream.flush()
            outputStream.close()

            val pdfUri = Uri.fromFile(File(path))

            if (pdfUri != null) {
                enableButton()
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Photos are converted and saved to Documents/AI-Image-Converter folder ",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                converter.gotoResult(pdfUri, "pdf")
            }

        } catch (e: Exception) {
            enableButton()
            runOnUiThread {
                Toast.makeText(this, "Unable to convert to PDF - ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
            e.printStackTrace()
        }

    }

}