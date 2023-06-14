package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.gson.Gson
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.FragmentHomeBinding


class Home : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAd()
        actions()
        pickImages()
        backPress()
    }


    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        binding.apply {
            mainAdView.loadAd(adRequest)
            secondAdView.loadAd(adRequest)
        }
    }

    private fun actions() {
        try {
            binding.header.headerIcon.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Introducing AI Image Converter, the ultimate app for converting images to various formats! Easily transform your images to PNG, JPG, JPEG, WebP, and PDF with just a few taps. Experience the power of image conversion and download AI Image Converter now: https://play.google.com/store/apps/details?id=image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai"
                        )
                    }
                    startActivity(Intent.createChooser(intent, "Share App"))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to Share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImages() {
        try {
            val maxLimit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                MediaStore.getPickImagesMaxLimit() - 1
            } else {
                50
            }

            val pickMultipleMedia =
                registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxLimit)) { uris ->
                    if (uris.isNotEmpty()) {

                        val imageUrisJson =
                            Gson().toJson(uris.map { uri -> uri.toString() })

                        val args = Bundle().apply {
                            putString("uris", imageUrisJson)
                        }
                        findNavController().navigate(R.id.action_home2_to_convertImagesPage, args)
                    }
                }

            binding.apply {
                selectImageView.setOnClickListener {
                    launchImagesPicker(pickMultipleMedia)
                }
                selectImagesBtn.setOnClickListener {
                    launchImagesPicker(pickMultipleMedia)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to select Images", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchImagesPicker(pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun backPress() {

        val onBackPressCallback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                val alertDialogBuilder = AlertDialog.Builder(requireContext())

                alertDialogBuilder.setTitle("Exit App")

                alertDialogBuilder.setMessage("Are you sure you want to exit?")

                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    ActivityCompat.finishAffinity(requireActivity())
                }

                alertDialogBuilder.setNegativeButton("No") { _, _ ->
                }

                alertDialogBuilder.show()

            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressCallback
        )

    }


}