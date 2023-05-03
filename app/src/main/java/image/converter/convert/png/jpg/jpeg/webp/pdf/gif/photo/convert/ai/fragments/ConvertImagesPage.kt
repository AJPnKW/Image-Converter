package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter.ImageAdapter
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.FragmentConvertImagesPageBinding
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services.ConverterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.properties.Delegates


class ConvertImagesPage : Fragment(R.layout.fragment_convert_images_page) {

    private var _binding: FragmentConvertImagesPageBinding? = null
    private val binding get() = _binding!!

    private val urisLiveData = MutableLiveData<List<Uri>?>()
    private var listSize by Delegates.notNull<Int>()
    private var editPosition: Int? = null
    private var quality: Int = 99
    private var format: String? = null
    private var fileName: String? = null
    private var pageSize = "Free Size"
    private var pdfCompression = 0
    private var navText = "Convert Images"

    private var pixWidth: Int? = null
    private var pixHeight: Int? = null

    private lateinit var imageAdapter: ImageAdapter
    private var isConverting = false

    private lateinit var converter: ConverterService
    private val thread = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConvertImagesPageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            converter = ConverterService(requireContext())
            getUris()
            setUpMainView()
            actions()
            pickNewImages()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Unable to get images", Toast.LENGTH_SHORT).show()
        }
    }


    private fun actions() {
        headerAction()
        selectFormatAction()
        changeCompressionLevel()
        convertImage()
    }

    @SuppressLint("SetTextI18n")
    private fun headerAction() {
        urisLiveData.observe(viewLifecycleOwner) {
            navText =
                if (listSize == 0) "Select Images ->" else if (listSize > 1) "Convert Images" else "Convert Image"
            binding.apply {
                header.apply {
                    headerText.text = navText
                    headerIcon.visibility = View.GONE
                    deleteAllImages.apply {
                        visibility = if (listSize != 0) View.VISIBLE else View.GONE
                        setOnClickListener {
                            deleteImages()
                        }
                    }
                }

                if (listSize == 0) {
                    updateUIVisibility(false, true, true)
                } else {
                    updateUIVisibility(true, false, false)
                }

            }
        }

        binding.enterFileName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                fileName = s.toString()
            }
        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUIVisibility(
        isVisible: Boolean,
        isImgNotFound: Boolean,
        hideBtn: Boolean,
    ) {
        with(binding) {
            imagesNotFound.visibility = if (isImgNotFound && !isVisible) View.VISIBLE else View.GONE
            enterFileName.visibility = if (isVisible) View.VISIBLE else View.GONE
            compressionLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            selectFormatLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            convertImageBtn.visibility = if (!hideBtn) View.VISIBLE else View.GONE

            header.apply {
                deleteAllImages.visibility =
                    if (!isConverting && listSize > 0) View.VISIBLE else View.GONE
                addMoreImage.visibility = if (!isConverting) View.VISIBLE else View.GONE
                headerText.text =
                    if (isConverting) "Converting Images..." else navText
            }
        }
        imageAdapter.updateIsConverting(isConverting)
        imageAdapter.notifyDataSetChanged()
    }


    @SuppressLint("InflateParams", "SetTextI18n")
    private fun selectFormatAction() {
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.format_dialog, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val imgSizePopup =
            LayoutInflater.from(requireContext()).inflate(R.layout.image_size_dialog, null)
        val imgSizePopupWindow = PopupWindow(
            imgSizePopup,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        binding.apply {
            selectFormat.setOnClickListener {
                popupWindow.showAsDropDown(selectFormat, 0, 0)
                val popupRadioGroup = popupView.findViewById<RadioGroup>(R.id.popupRadioGroup)
                popupRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val selectedRadioButton = popupView.findViewById<RadioButton>(checkedId)
                    if (selectedRadioButton != null) {
                        val value = selectedRadioButton.text.toString()
                        selectFormat.text = value
                        format = value

                        compressionLayout.visibility = View.VISIBLE
                        compressionLevel.values = listOf(1.0f)

                        convertImageBtn.isEnabled = true

                        if (value == "PDF") {
                            pdfPageSize.visibility = View.VISIBLE
                            selectSize.visibility = View.GONE
                        } else {
                            pdfPageSize.visibility = View.GONE
                            selectSize.visibility = View.VISIBLE
                        }

                    }
                    popupWindow.dismiss()
                }


            }

            pdfPageSize.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        pageSize = pdfPageSize.selectedItem.toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }

            selectSize.setOnClickListener {
                imgSizePopupWindow.showAsDropDown(selectSize, 0, 0)
                val imgSizePopupRadioGroup =
                    imgSizePopup.findViewById<RadioGroup>(R.id.imgSizeRadio)
                imgSizePopupRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val selectedImgRadioButton = imgSizePopup.findViewById<RadioButton>(checkedId)
                    if (selectedImgRadioButton != null) {
                        val value = selectedImgRadioButton.text.toString()
                        if (value != "Original") {
                            val dimens = value.split(" ").map { it.trim() }
                            pixWidth = dimens[0].toInt()
                            pixHeight = dimens[2].toInt()
                            selectSize.text = "$pixWidth x $pixHeight"
                        } else {
                            selectSize.text = value
                            pixWidth = null
                            pixHeight = null
                        }
                    }
                    imgSizePopupWindow.dismiss()
                }

            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun changeCompressionLevel() {
        binding.apply {
            compressionLevel.apply {
                setLabelFormatter { "" }
                addOnChangeListener { _, value, _ ->
                    val intValue = value.toInt()
                    quality = 101 - intValue
                    if (intValue >= 10) {
                        pdfCompression = converter.changeQuality(intValue)
                    }
                    compressionLevelText.text = "Compression Level: $intValue %"
                }
            }
        }
    }

    private fun pickNewImages() {
        val maxItem =
            if (format == "PDF") 500 - urisLiveData.value?.size!! else 50 - urisLiveData.value?.size!!

        val pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItem)) { newUris ->
                if (newUris.isNotEmpty()) {
                    urisLiveData.value = (urisLiveData.value ?: emptyList()) + newUris
                }
            }
        binding.header.addMoreImage.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun deleteImages() {
        val alert = AlertDialog.Builder(requireContext())
        alert.apply {
            setTitle("Delete Images")
            setMessage("Are you sure you want to delete all Images?")
            setPositiveButton("Yes") { _, _ ->
                urisLiveData.value = emptyList()
            }
            setNegativeButton("No") { _, _ -> }
            show()
        }
    }

    private fun getUris() {
        val urisString = arguments?.getString("uris")
        if (urisString != null) {
            val imageUrisStrings: List<String> =
                Gson().fromJson(urisString, object : TypeToken<List<String>>() {}.type)
            val uris = imageUrisStrings.map { Uri.parse(it) }
            listSize = uris.size
            urisLiveData.value = uris

        }
    }

    private fun removeItemAt(position: Int) {
        val currentList = urisLiveData.value?.toMutableList()
        if (currentList != null && currentList.size > position) {
            currentList.removeAt(position)
            urisLiveData.postValue(currentList)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                editPosition?.let {
                    addUriAtIndex(resultUri, it)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                error.printStackTrace()
            }
        }
    }

    private fun addUriAtIndex(uri: Uri, index: Int) {
        val currentList = urisLiveData.value?.toMutableList() ?: mutableListOf()
        currentList.removeAt(index)
        currentList.add(index, uri)
        urisLiveData.postValue(currentList)
    }


    private fun editImage(position: Int) {
        val currentList = urisLiveData.value?.toMutableList()
        if (currentList != null && currentList.size > position) {
            val img = currentList[position]
            editPosition = position
            CropImage.activity(img).setGuidelines(CropImageView.Guidelines.ON)
                .start(requireContext(), this)
        }
    }


    private fun setUpMainView() {
        val imgLayout = GridLayoutManager(requireContext(), 3)
        imageAdapter =
            ImageAdapter(requireContext(), ::removeItemAt, ::editImage, isConverting)

        imgLayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1 // Return 1 for each item, assuming each item occupies 1 span
            }
        }

        binding.showImagesRecyclerView.apply {
            layoutManager = imgLayout
            adapter = imageAdapter
        }

        activity?.let {
            urisLiveData.observe(viewLifecycleOwner) { uris ->
                imageAdapter.differ.submitList(uris)
                listSize = uris?.size!!
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private fun convertImage() {
        binding.convertImageBtn.apply {
            setOnClickListener {
                if (format == "PDF") {
                    thread.launch {
                        convertToPdf()
                    }
                } else {
                    thread.launch {
                        urisLiveData.value?.forEachIndexed { index, uri ->
                            convertImageToFormat(uri, index)
                        }
                    }
                }
                binding.progressBar.apply {
                    max = 100
                    visibility = View.VISIBLE
                }
                isEnabled = false
                text = "Converting..."
                isConverting = true
                updateUIVisibility(false, false, false)
            }
        }
    }


    private fun convertImageToFormat(imageUri: Uri, index: Int) {
        if (format != null) {
            val newImageUri: Uri?
            val imageName =
                if (fileName != null && fileName!!.trim().isNotEmpty() && fileName!!.trim()
                        .isNotBlank()
                ) "${fileName}_$index" else "ai-converted-image${index}-${System.currentTimeMillis()}"
            val path = converter.getOutputPath(imageName, format!!.lowercase())
            val outputStream = FileOutputStream(path)

            val bitmap = converter.getBitmapFromUri(imageUri)

            val scaledBitmap =
                if (pixHeight != null && pixWidth != null) Bitmap.createScaledBitmap(
                    bitmap!!,
                    pixWidth!!, pixHeight!!, false
                ) else bitmap

            val rotatedBitmap = converter.rotateBitmapIfRequired(scaledBitmap!!, imageUri)
            rotatedBitmap?.compress(
                converter.getBitmapFormat(format!!.lowercase()),
                quality,
                outputStream
            )


            outputStream.flush()
            outputStream.close()

            newImageUri = if (format == "JPG") {
                val outputFile = File(path)
                val newOutputPath = path.replace(".jpeg", ".jpg")
                outputFile.renameTo(File(newOutputPath))
                Uri.fromFile(File(newOutputPath))
            } else {
                Uri.fromFile(File(path))
            }

            if (newImageUri != null && index + 1 == urisLiveData.value?.size) {
                showSuccessMsg()
            } else {
                showProgress(index)
            }

        }
    }

    private fun convertToPdf() {
        val imageUris = urisLiveData.value!!

        val imgName =
            if (fileName != null && fileName!!.trim().isNotEmpty() && fileName!!.trim()
                    .isNotBlank()
            ) fileName!! else "ai-converted-image-${System.currentTimeMillis()}"

        val path = converter.getOutputPath(imgName, "pdf")
        val outputStream = FileOutputStream(path)
        val document = Document()
        val pdfWriter = PdfWriter.getInstance(document, outputStream)
        pdfWriter.compressionLevel = pdfCompression
        document.open()

        val isSingle = imageUris.size < 2
        for ((index, imageUri) in imageUris.withIndex()) {
            converter.convertImgToPdf(document, imageUri, pageSize, isSingle)
            showProgress(index)
        }

        document.close()
        pdfWriter.close()

        outputStream.flush()
        outputStream.close()

        val pdfUri = Uri.fromFile(File(path))

        if (pdfUri != null) {
            showSuccessMsg()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showSuccessMsg() {
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                "Photo converted and saved to Documents/AI-Image-Converter folder ",
                Toast.LENGTH_LONG
            )
                .show()

            binding.apply {
                convertImageBtn.apply {
                    isEnabled = true
                    text = "CONVERT IMAGE"
                }
                progressBar.apply {
                    progress = 0
                    visibility = View.GONE
                }
            }
            isConverting = false
            updateUIVisibility(true, false, false)

        }
    }

    private fun showProgress(index: Int) {
        activity?.runOnUiThread {
            binding.progressBar.progress = (index + 1) * 100 / urisLiveData.value!!.size
        }
    }

}