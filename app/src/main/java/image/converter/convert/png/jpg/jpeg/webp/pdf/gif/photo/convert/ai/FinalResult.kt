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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityFinalResultBinding
import java.io.File


class FinalResult : AppCompatActivity() {

    private lateinit var binding: ActivityFinalResultBinding
    private lateinit var fileUri: Uri
    private lateinit var fileType: String

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        setUpAd()

        setResultData()
        actions()

    }

    private fun setUpAd() {
        try {

            val interAdRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                this@FinalResult,
                "ca-app-pub-3516566345027334/1848466739",
                interAdRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                    }
                })

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

                mInterstitialAd?.show(this@FinalResult)

                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        mInterstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        mInterstitialAd = null
                    }
                }

                if (mInterstitialAd == null) {
                    goToMain()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
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