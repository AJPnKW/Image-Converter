package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter


import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ImageItemsBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.viewholder.ImagesViewHolder

class ImageAdapter(
    private val context: Context,
    private val removeItemAt: (position: Int) -> Unit,
    private val editImage: (position: Int) -> Unit,
    private var isConverting: Boolean
) :
    RecyclerView.Adapter<ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        return ImagesViewHolder(
            ImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            removeItemAt,
            editImage
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

    fun updateIsConverting(isConverting: Boolean) {
        this.isConverting = isConverting
    }

    val differ = AsyncListDiffer(this, differCAllBack)


    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val img = differ.currentList[position]
        Glide.with(context).load(img).placeholder(R.drawable.loading).into(holder.binding.image)
        holder.binding.apply {
            editIcon.visibility = if (isConverting) View.GONE else View.VISIBLE
            deleteIcon.visibility = if (isConverting) View.GONE else View.VISIBLE
        }
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}
