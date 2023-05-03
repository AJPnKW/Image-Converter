package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Utils {
    const val REQUEST_PERMISSION_CODE = 100
}

class AppService(private val context: Context, private val activity: Activity) {


    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun isAllPermissionsAllowed(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(
                activity.baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            requiredPermissions, Utils.REQUEST_PERMISSION_CODE
        )
    }


}