package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.ImageItemsBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.viewholder.ImagesViewHolder

class ImageAdapter() :
    RecyclerView.Adapter<ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        return ImagesViewHolder(
            ImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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


    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val img = differ.currentList[position]
        holder.binding.pdfImage.setImageURI(img)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val imageView: ImageView
//        if (convertView == null) {
//            // Inflate the layout for each item in the GridView
//            imageView = ImageView(context)
//            imageView.layoutParams = AbsListView.LayoutParams(250, 250) // Set image size as needed
//            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//            imageView.setPadding(8, 8, 8, 8)
//        } else {
//            imageView = convertView as ImageView
//        }
//
//        // Load the image from the Uri using Glide or any other image loading library
//        imageView.setImageURI(imageUris[position])
//
//        return imageView
//    }
}
