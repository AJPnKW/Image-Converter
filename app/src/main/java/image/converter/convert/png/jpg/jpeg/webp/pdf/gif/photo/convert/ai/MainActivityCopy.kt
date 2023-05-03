package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai
//
//import android.app.Activity
//import android.content.Intent
//import android.Manifest
//import android.app.AlertDialog
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.github.dhaval2404.imagepicker.ImagePicker
//import com.google.android.gms.ads.*
//import com.google.android.gms.ads.interstitial.InterstitialAd
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.google.gson.Gson
//import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityMainBinding
//
//
//class MainActivityCopy : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    private val permissions = arrayOf(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    )
//
//    private val isLowerBuildVersion = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
//
//    private var mInterstitialAd: InterstitialAd? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        MobileAds.initialize(this) {}
//        setUpAd()
//
//        if (!isAllPermissionGranted() && isLowerBuildVersion) {
//            requestPermissions()
//        }
//        binding.selectPhoto.setOnClickListener() {
//            if (isLowerBuildVersion) {
//                if (!isAllPermissionGranted()) {
//                    requestPermissions()
//                } else {
//                    pickImage()
//                }
//            } else {
//                pickImage()
//            }
//
//        }
//
//        binding.imagesToPdf.setOnClickListener() {
//            if (isLowerBuildVersion) {
//                if (!isAllPermissionGranted()) {
//                    requestPermissions()
//                } else {
//                    pickImages()
//                }
//            } else {
//                pickImages()
//            }
//
//        }
//        backPress()
//    }
//
//
//    private fun requestPermissions() {
//        ActivityCompat.requestPermissions(
//            this, permissions,
//            100
//        )
//    }
//
//    private fun isAllPermissionGranted(): Boolean {
//        return permissions.all {
//            ContextCompat.checkSelfPermission(
//                baseContext, it
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    private fun setUpAd() {
//        try {
//
//            val interAdRequest = AdRequest.Builder().build()
//            InterstitialAd.load(
//                this@MainActivityCopy,
//                "ca-app-pub-3516566345027334/1848466739",
//                interAdRequest,
//                object : InterstitialAdLoadCallback() {
//                    override fun onAdFailedToLoad(adError: LoadAdError) {
//                        mInterstitialAd = null
//                    }
//
//                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                        mInterstitialAd = interstitialAd
//                    }
//                })
//
//            val adRequest = AdRequest.Builder().build()
//            val adRequestTwo = AdRequest.Builder().build()
//            val adRequestThree = AdRequest.Builder().build()
//
//            binding.apply {
//                mainAdView.apply {
//                    loadAd(adRequest)
//                    adListener = object : AdListener() {
//                        override fun onAdFailedToLoad(adError: LoadAdError) {
//                            Toast.makeText(
//                                context,
//                                "Turn on Internet connection for better performance! 🙂",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//                }
//                secondAdView.loadAd(adRequestTwo)
//                thirdAdView.loadAd(adRequestThree)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode != 100 || isAllPermissionGranted()) {
//            Toast.makeText(this, "Please allow the permissions", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private val startForImageResult =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            when (it.resultCode) {
//                Activity.RESULT_OK -> {
//                    val imageUri = it.data?.data
//                    if (imageUri != null) {
//                        val i = Intent(this, ImageConverter::class.java)
//                        i.putExtra("imageUrl", imageUri.toString())
//                        startActivity(i)
//                    }
//                }
//                ImagePicker.RESULT_ERROR -> {
//                    Toast.makeText(this, ImagePicker.getError(it.data), Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//
//    private val startForImagesResult =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            when (it.resultCode) {
//                Activity.RESULT_OK -> {
//                    val clipData = it.data?.clipData
//                    if (clipData != null) {
//                        val imageUris = mutableListOf<Uri>()
//                        val size = clipData.itemCount
//                        for (i in 0 until size) {
//                            val item = clipData.getItemAt(i)
//                            val uri = item.uri
//                            imageUris.add(uri)
//                        }
//
//                        val imageUrisJson =
//                            Gson().toJson(imageUris.map { uri -> uri.toString() })
//                        val i = Intent(this, ImagesTOPdf::class.java)
//                        i.putExtra("imageUrisJson", imageUrisJson)
//                        startActivity(i)
//                    } else {
//                        Toast.makeText(this, "Please select more than 1 images", Toast.LENGTH_LONG)
//                            .show()
//                    }
//
//                }
//            }
//        }
//
//
//    private fun pickImage() {
//        ImagePicker.with(this).crop().createIntent { intent ->
//            startForImageResult.launch(intent)
//        }
//    }
//
//    private fun pickImages() {
//        try {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            startForImagesResult.launch(intent)
//        } catch (e: Exception) {
//            Toast.makeText(this, "Unable to select pictures", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    private fun showExitAd() {
//        mInterstitialAd?.show(this)
//        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//            override fun onAdDismissedFullScreenContent() {
//                mInterstitialAd = null
//            }
//
//            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                mInterstitialAd = null
//            }
//        }
//    }
//
//    private fun backPress() {
//        val onBackPressCallback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                val alertDialogBuilder = AlertDialog.Builder(this@MainActivityCopy)
//                alertDialogBuilder.setTitle("Exit App")
//                alertDialogBuilder.setMessage("Are you sure you want to exit?")
//                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
//                    finishAffinity()
//                }
//                alertDialogBuilder.setNegativeButton("No") { _, _ ->
//                    showExitAd()
//                }
//                alertDialogBuilder.show()
//            }
//        }
//        onBackPressedDispatcher.addCallback(this, onBackPressCallback)
//    }
//
//}