package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ResultImageItemBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.viewholder.FinalResultImgViewHolder

class FinalResultAdapter(
    private val context: Context,
    private val openImage: (position: Int) -> Unit,
) :
    RecyclerView.Adapter<FinalResultImgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinalResultImgViewHolder {
        return FinalResultImgViewHolder(
            ResultImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            openImage
        )
    }

    private val differCAllBack = object : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCAllBack)

    override fun onBindViewHolder(holder: FinalResultImgViewHolder, position: Int) {
        val img = differ.currentList[position]
        Glide.with(context).load(img).placeholder(R.drawable.loading)
            .into(holder.binding.resultImage)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}