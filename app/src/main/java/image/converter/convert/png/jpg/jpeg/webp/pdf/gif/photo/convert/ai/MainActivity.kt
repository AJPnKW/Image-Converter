package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai


import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ActivityMainBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services.AppService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appService = AppService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!appService.isAllPermissionsAllowed()) {
                appService.requestPermissions()
            }
        }

    }
}