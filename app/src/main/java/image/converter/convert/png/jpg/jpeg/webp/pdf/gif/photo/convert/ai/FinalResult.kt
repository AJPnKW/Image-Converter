package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityFinalResultBinding
import java.io.File


class FinalResult : AppCompatActivity() {

    private lateinit var binding: ActivityFinalResultBinding
    private lateinit var fileUri: Uri
    private lateinit var fileType: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setResultData()
        actions()

    }

    private fun actions() {
        binding.apply {
            image.setOnClickListener() {
                openUri(fileUri, fileType)
            }
            pdf.setOnClickListener() {
                openUri(fileUri, fileType)
            }
            share.setOnClickListener() {
                shareFile()
            }
            close.setOnClickListener() {
                startActivity(Intent(this@FinalResult, MainActivity::class.java))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setResultData() {
        val uri = intent.extras?.getString("uri")
        val type = intent.extras?.getString("type")

        if (type != null && uri != null) {

            val loadUri = Uri.parse(uri)

            fileUri = loadUri
            fileType = type


            if (type == "image") {

                binding.apply {
                    image.visibility = View.VISIBLE
                    resultText.text = "Photo Saved Successfully"
                    resultImage.apply {
                        visibility = View.VISIBLE
                        setImageURI(loadUri)
                    }
                }


            } else if (type == "pdf") {
                binding.apply {
                    pdf.visibility = View.VISIBLE
                    resultText.text = "Pdf Saved Successfully"
                    resultPdf.apply {
                        visibility = View.VISIBLE
                        fromUri(loadUri).defaultPage(0).enableSwipe(true)
                            .swipeHorizontal(false).onError { t ->
                                Toast.makeText(
                                    this@FinalResult,
                                    "Failed to load PDF: ${t.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.load()
                    }
                }

            }
        }

    }


    private fun openUri(uri: Uri, type: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)

            when (type) {
                "image" -> intent.setDataAndType(uri, "image/*")
                "pdf" -> intent.setDataAndType(uri, "application/pdf")
                else -> {
                    Toast.makeText(this, "Unknown type", Toast.LENGTH_SHORT).show()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider to create a content URI for file
                val contentUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    File(uri.path!!)
                )
                intent.setDataAndType(contentUri, intent.type)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Could not open the $type", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    private fun shareFile() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            when (fileType) {
                "image" -> shareIntent.type = "image/*"
                "pdf" -> shareIntent.type = "application/pdf"
                else -> {
                    Toast.makeText(this, "Unknown type", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            val authority = BuildConfig.APPLICATION_ID + ".provider"

            val contentUri = FileProvider.getUriForFile(
                this,
                authority,
                File(fileUri.path!!)
            )

            grantUriPermission(
                packageName,
                contentUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "File could not be shared!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


}