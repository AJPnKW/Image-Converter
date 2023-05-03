package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.FragmentFinalResultPageBinding


class FinalResultPage : Fragment(R.layout.fragment_final_result_page) {

    private var _binding: FragmentFinalResultPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinalResultPageBinding.inflate(inflater, container, false)
        return binding.root
    }

}