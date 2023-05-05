package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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

    private var results = mutableListOf<Uri>()

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConvertImagesPageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAd()
        try {
            converter = ConverterService(requireContext())
            getUris()
            setUpMainView()
            actions()
            pickNewImages()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to get images", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-3516566345027334/6440846651",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )

        binding.secondAd.loadAd(adRequest)
    }


    private fun actions() {
        headerAction()
        selectFormatAction()
        changeCompressionLevel()
        convertImage()
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

    @SuppressLint("SetTextI18n")
    private fun headerAction() {
        urisLiveData.observe(viewLifecycleOwner) { uris ->
            val liveListSize = uris?.size ?: listSize
            navText = when {
                liveListSize == 0 -> "Select Images ->"
                liveListSize > 1 -> "Convert Images"
                else -> "Convert Image"
            }
            binding.header.apply {
                headerText.text = navText
                headerIcon.visibility = View.GONE
                deleteAllImages.apply {
                    visibility = if (liveListSize != 0) View.VISIBLE else View.GONE
                    setOnClickListener { deleteImages() }
                }
            }
            updateUIVisibility(liveListSize != 0, liveListSize == 0, liveListSize == 0)
        }
        binding.enterFileName.doAfterTextChanged { fileName = it.toString() }
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
            500,
            500,
            true
        )

        val imgSizePopup =
            LayoutInflater.from(requireContext()).inflate(R.layout.image_size_dialog, null)
        val imgSizePopupWindow = PopupWindow(
            imgSizePopup,
            800,
            1000,
            true
        )

        with(binding) {
            selectFormat.setOnClickListener {
                popupWindow.showAsDropDown(selectFormat, 0, 0)

                popupView.findViewById<RadioGroup>(R.id.popupRadioGroup)
                    .setOnCheckedChangeListener { _, checkedId ->
                        val selectedRadioButton = popupView.findViewById<RadioButton>(checkedId)
                        if (selectedRadioButton != null) {
                            format = selectedRadioButton.text.toString()
                            selectFormat.text = format

                            compressionLayout.visibility = View.VISIBLE
                            compressionLevel.values = listOf(1.0f)
                            convertImageBtn.isEnabled = true

                            pdfPageSize.visibility =
                                if (format == "PDF") View.VISIBLE else View.GONE
                            selectSize.visibility = if (format == "PDF") View.GONE else View.VISIBLE
                        }
                        popupWindow.dismiss()
                    }
            }

            pdfPageSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pageSize = pdfPageSize.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            selectSize.setOnClickListener {
                imgSizePopupWindow.showAsDropDown(selectSize, 0, 0)

                imgSizePopup.findViewById<RadioGroup>(R.id.imgSizeRadio)
                    .setOnCheckedChangeListener { _, checkedId ->
                        val selectedImgRadioButton =
                            imgSizePopup.findViewById<RadioButton>(checkedId)
                        if (selectedImgRadioButton != null) {
                            val value = selectedImgRadioButton.text.toString()
                            if (value != "Original") {
                                val arrValue = value.split(" ")
                                pixWidth = arrValue[0].toInt()
                                pixHeight = arrValue[2].toInt()
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
        binding.compressionLevel.apply {
            setLabelFormatter { "" }
            addOnChangeListener { _, value, _ ->
                val intValue = value.toInt()
                binding.apply {
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
        val pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(50)) { newUris ->
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
                return 1
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
                results = mutableListOf()
                thread.launch {
                    when (format) {
                        "PDF" -> convertToPdf()
                        else -> urisLiveData.value?.forEachIndexed { index, uri ->
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
        try {
            format?.let { format ->
                val newImageUri: Uri?
                val imageName = fileName?.takeIf { it.isNotBlank() }?.let { "${it}_$index" }
                    ?: "ai-converted-image-$index-${System.currentTimeMillis()}"
                val path = converter.getOutputPath(imageName, format.lowercase())
                val outputStream = FileOutputStream(path)
                val bitmap = converter.getBitmapFromUri(imageUri)
                val scaledBitmap = pixHeight?.let { height ->
                    pixWidth?.let { width ->
                        Bitmap.createScaledBitmap(bitmap!!, width, height, false)
                    }
                } ?: bitmap
                val rotatedBitmap = converter.rotateBitmapIfRequired(scaledBitmap!!, imageUri)
                rotatedBitmap?.compress(
                    converter.getBitmapFormat(format.lowercase()),
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
                newImageUri?.let {
                    results.add(it)
                    if (index + 1 == urisLiveData.value?.size) {
                        showSuccessMsg()
                    } else {
                        showProgress(index)
                    }
                }
            }
        } catch (e: Exception) {
            activity?.runOnUiThread {
                convertingError()
            }
        }
    }


    private fun convertToPdf() {
        try {
            val imageUris = urisLiveData.value!!
            val imgName = fileName?.takeIf { it.isNotBlank() && it.isNotEmpty() }
                ?: "ai-converted-image-${System.currentTimeMillis()}"

            val path = converter.getOutputPath(imgName, "pdf")

            val outputStream = FileOutputStream(path)
            val document = Document()
            val pdfWriter = PdfWriter.getInstance(document, outputStream).apply {
                compressionLevel = pdfCompression
            }
            document.open()

            val isSingle = imageUris.size < 2
            imageUris.forEachIndexed { index, imageUri ->
                converter.convertImgToPdf(document, imageUri, pageSize, isSingle)
                showProgress(index)
            }

            document.close()
            pdfWriter.close()
            outputStream.close()

            val pdfUri = Uri.fromFile(File(path))
            if (pdfUri != null) {
                results.add(pdfUri)
                showSuccessMsg()
            }
        } catch (e: Exception) {
            convertingError()
        }
    }

    private fun convertingError() {
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                "Error while converting image to $format",
                Toast.LENGTH_LONG
            ).show()
            isConverting = false
            updateUIVisibility(true, false, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSuccessMsg() {
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                "Photo converted and saved to Documents/AI-Image-Converter folder",
                Toast.LENGTH_LONG
            ).show()

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
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(requireActivity())
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        navigateToFinalResultPage()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        navigateToFinalResultPage()
                    }

                }
            } else {
                navigateToFinalResultPage()
            }

        }
    }

    private fun navigateToFinalResultPage() {
        if (results.isNotEmpty()) {
            val resultsJson =
                Gson().toJson(results.map { uri -> uri.toString() })

            val args = Bundle().apply {
                putString("results", resultsJson)
                putString("format", format)
            }


            findNavController().navigate(
                R.id.action_convertImagesPage_to_finalResultPage,
                args
            )

        }
    }


    private fun showProgress(index: Int) {
        activity?.runOnUiThread {
            binding.header.headerText.text =
                if (isConverting) "Converting Images...($index / $listSize)" else navText
            binding.progressBar.progress = (index + 1) * 100 / urisLiveData.value!!.size
        }
    }

}