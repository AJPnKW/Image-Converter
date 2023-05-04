package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.viewholder

import androidx.recyclerview.widget.RecyclerView
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ResultImageItemBinding

class FinalResultImgViewHolder(
    val binding: ResultImageItemBinding,
    private val openImage: (position: Int) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.resultImage.setOnClickListener {
            openImage(adapterPosition)
        }
    }
}