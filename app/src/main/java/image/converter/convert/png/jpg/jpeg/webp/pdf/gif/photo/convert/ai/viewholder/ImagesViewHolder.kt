package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ImageItemsBinding

class ImagesViewHolder(
    val binding: ImageItemsBinding,
    private val removeItemAt: (position: Int) -> Unit,
    private val editImage: (position: Int) -> Unit,

) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.apply {

            deleteIcon.apply {
//                visibility = if (isConverting) View.GONE else View.VISIBLE
                setOnClickListener {
                    removeItemAt(adapterPosition)
                }
            }
            editIcon.apply {
//                visibility = if (isConverting) View.GONE else View.VISIBLE
                setOnClickListener {
                    editImage(adapterPosition)
                }
            }

        }


    }


}