package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.BuildConfig
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.R
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.adapter.FinalResultAdapter
import image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.databinding.FragmentFinalResultPageBinding
import java.io.File


class FinalResultPage : Fragment(R.layout.fragment_final_result_page) {

    private var _binding: FragmentFinalResultPageBinding? = null
    private val binding get() = _binding!!
    private val resultLiveData = MutableLiveData<List<Uri>?>()
    private var format: String? = null
    private var resultSize: Int = 0

    private var appOpenAd: AppOpenAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinalResultPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAd()
        try {
            getData()
            actions()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            requireContext(),
            "ca-app-pub-3516566345027334/5127764984",
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    appOpenAd = null
                }
            })


        binding.apply {
            firstAd.loadAd(adRequest)
            secondAd.loadAd(adRequest)
            thirdAd.loadAd(adRequest)
        }
    }

    private fun getData() {
        val resultUrisString = arguments?.getString("results")
        format = arguments?.getString("format")
        if (resultUrisString != null) {
            val uriString: List<String> =
                Gson().fromJson(resultUrisString, object : TypeToken<List<String>>() {}.type)
            val uris = uriString.map { Uri.parse(it) }
            resultSize = uris.size
            resultLiveData.value = uris
        }

    }

    @SuppressLint("SetTextI18n")
    private fun actions() {
        val isPdf = format == "PDF"
        val uri = resultLiveData.value?.get(0)

        binding.apply {
            header.apply {
                headerText.text = "Result ($format)"
                headerIcon.visibility = View.GONE
                backToHome.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        showOpenAd()
                    }
                }
            }


            openPdf.apply {
                visibility = if (isPdf) View.VISIBLE else View.GONE
                setOnClickListener {
                    sharePdf(uri!!, true)
                }
            }
            openImage.apply {
                visibility = if (isPdf || resultSize > 1) View.GONE else View.VISIBLE
                setOnClickListener {
                    shareImages(true)
                }
            }
            resultText.text = when {
                isPdf -> "PDF saved successfully"
                resultSize > 1 -> "Images saved successfully"
                else -> "Image saved successfully"
            }
            share.setOnClickListener {
                if (isPdf) {
                    sharePdf(uri!!)
                } else {
                    shareImages()
                }
            }


        }
        if (isPdf) {
            setUpPdf(uri!!)
        } else {
            setUpImages()
        }
    }

    private fun openImage(position: Int) {
        val uri = resultLiveData.value?.get(position)
        shareImages(true, uri)
    }

    private fun setUpPdf(uri: Uri) {
        binding.resultPdf.apply {
            visibility = View.VISIBLE
            fromUri(uri).defaultPage(0).enableSwipe(true).swipeHorizontal(false)
                .onError { err ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to load PDF: ${err.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }.load()
        }

    }

    private fun sharePdf(uri: Uri, isView: Boolean = false) {
        try {
            val context = requireContext()
            val file = uri.path?.let { File(it) }
            val extraMimeTypes = arrayOf("application/pdf", "text/plain")

            if (file != null) {
                if (file.exists()) {
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    val actionType = if (isView) Intent.ACTION_VIEW else Intent.ACTION_SEND
                    val intent = Intent(actionType).apply {
                        clipData = ClipData.newRawUri("", fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    val title: String
                    if (!isView) {
                        title = "Share PDF"
                        intent.apply {
                            type = "*/*"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Hi, I've converted images to $format using AI Image Converter. Check out this cool app for converting images to $format: https://play.google.com/store/apps/details?id=image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai"
                            )
                        }
                    } else {
                        title = "Open PDF"
                        intent.setDataAndType(fileUri, "application/pdf")
                    }

                    startActivity(Intent.createChooser(intent, title))
                }
            }

        } catch (e: Exception) {
            showError()
        }
    }

    private fun shareImages(showImage: Boolean = false, uri: Uri? = null) {
        try {
            val uris = resultLiveData.value!!
            val extraMimeTypes = arrayOf("image/*", "text/plain")
            val fileUris = mutableListOf<Uri>()
            val cdata = ClipData.newRawUri("", null)

            if (uri == null) {
                uris.forEach { u ->
                    val fileUri = addToFileUris(fileUris, u)
                    cdata.addItem(ClipData.Item(fileUri))
                }
            } else {
                val fileUri = addToFileUris(fileUris, uri)
                cdata.addItem(ClipData.Item(fileUri))
            }

            val actionType = if (showImage) Intent.ACTION_VIEW else Intent.ACTION_SEND_MULTIPLE

            val intent = Intent(actionType).apply {
                clipData = cdata
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            }
            val title: String

            if (!showImage) {
                title = "Share $format"
                intent.apply {
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(fileUris))
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Hi, I've converted images to $format using AI Image Converter. Check out this cool app for converting images to $format: https://play.google.com/store/apps/details?id=image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai"
                    )
                }
            } else {
                title = "Open Image"
                intent.setDataAndType(fileUris[0], "image/*")
            }


            startActivity(Intent.createChooser(intent, title))

        } catch (e: Exception) {
            showError()
        }
    }

    private fun addToFileUris(fileUris: MutableList<Uri>, uri: Uri): Uri {
        val file = uri.path?.let { File(it) }
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file!!
        )
        fileUris.add(fileUri)
        return fileUri
    }


    private fun setUpImages() {

        if (resultSize > 1) {
            val imgLayout = GridLayoutManager(requireContext(), 3)
            val imageAdapter = FinalResultAdapter(requireContext(), ::openImage)
            imgLayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1
                }
            }
            binding.apply {
                finalResultScroll.visibility = View.VISIBLE
                finalResultRecyclerView.apply {
                    layoutManager = imgLayout
                    adapter = imageAdapter
                }
            }
            activity?.let {
                resultLiveData.observe(viewLifecycleOwner) {
                    imageAdapter.differ.submitList(it)
                }
            }
        } else {
            val uri = resultLiveData.value?.get(0)
            binding.singleImg.visibility = View.VISIBLE
            Glide.with(requireContext()).load(uri).placeholder(R.drawable.loading)
                .into(binding.singleImg)
        }


    }

    private fun showOpenAd() {
        if (appOpenAd != null) {
            appOpenAd!!.show(requireActivity())

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    goToHome()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    goToHome()
                }
            }

        } else {
            goToHome()
        }
    }

    private fun goToHome() {
        findNavController().navigate(R.id.action_finalResultPage_to_home2)
    }

    private fun showError() {
        Toast.makeText(requireContext(), "Unable to share $format", Toast.LENGTH_LONG).show()
    }

}