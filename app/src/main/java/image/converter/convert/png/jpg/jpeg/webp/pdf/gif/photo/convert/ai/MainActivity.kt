package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai

import android.app.Activity
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val isLowerBuildVersion = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isAllPermissionGranted() && isLowerBuildVersion) {
            requestPermissions()
        }
        binding.selectPhoto.setOnClickListener() {
            if (isLowerBuildVersion) {
                if (!isAllPermissionGranted()) {
                    requestPermissions()
                }
            } else {
                pickImage()
            }

        }

        binding.imagesToPdf.setOnClickListener() {
            if (isLowerBuildVersion) {
                if (!isAllPermissionGranted()) {
                    requestPermissions()
                }
            } else {
                pickImages()
            }

        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, permissions,
            100
        )
    }

    private fun isAllPermissionGranted(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 100 || isAllPermissionGranted()) {
            Toast.makeText(this, "Please allow the permissions", Toast.LENGTH_LONG).show()
        }
    }

    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri = it.data?.data
                    if (imageUri != null) {
                        val i = Intent(this, ImageConverter::class.java)
                        i.putExtra("imageUrl", imageUri.toString())
                        startActivity(i)
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(it.data), Toast.LENGTH_LONG).show()
                }
            }
        }

    private val startForImagesResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val imageUris = mutableListOf<Uri>()
                    val clipData = it.data?.clipData
                    val data = it.data?.data
                    if (clipData != null) {
                        val size = clipData.itemCount
                        if (size > 15) {
                            Toast.makeText(this, "Please select up-to 15 images", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            for (i in 0 until size) {
                                val item = clipData.getItemAt(i)
                                val uri = item.uri
                                imageUris.add(uri)
                            }
                        }

                    } else if (data != null) {
                        imageUris.add(data)
                    }

                    if (imageUris.size > 0) {
                        val imageUrisJson = Gson().toJson(imageUris.map { uri -> uri.toString() })
                        val i = Intent(this, ImagesTOPdf::class.java)
                        i.putExtra("imageUrisJson", imageUrisJson)
                        startActivity(i)

                    } else {
                        Toast.makeText(this, "Please select images", Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    Toast.makeText(this, "Unable to pick images", Toast.LENGTH_LONG).show()
                }
            }
        }


    private fun pickImage() {
        ImagePicker.with(this).crop().createIntent { intent ->
            startForImageResult.launch(intent)
        }
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startForImagesResult.launch(intent)
    }
}