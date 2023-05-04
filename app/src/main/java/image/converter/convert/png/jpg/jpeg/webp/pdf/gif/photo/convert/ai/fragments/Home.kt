package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        pickImages()
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


}