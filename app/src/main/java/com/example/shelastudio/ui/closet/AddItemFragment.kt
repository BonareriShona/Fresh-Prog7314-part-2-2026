package com.example.shelastudio.ui.closet

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentAddItemBinding
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddItemFragment : Fragment() {

    companion object {
        private const val TAG = "AddItemFragment"
    }

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WardrobeViewModel by viewModels()

    private var pendingImageBase64: String? = null
    private var pendingCameraUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d(TAG, "Camera returned success=$success")
        if (success) {
            pendingCameraUri?.let { processImage(it) }
        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d(TAG, "Gallery returned uri=$uri")
        uri?.let { processImage(it) }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "Camera permission granted")
            launchCamera()
        } else {
            Log.w(TAG, "Camera permission denied")
            Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        setupDropdowns()
        setupSlider()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        val categories = listOf(
            "Tops", "Bottoms", "Dresses", "Shoes", "Bags", "Accessories", "Outerwear"
        )
        binding.actCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        )

        val seasons = listOf("Summer", "Winter", "All-Season")
        binding.actSeason.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, seasons)
        )
    }

    private fun setupSlider() {
        binding.sliderWarmth.addOnChangeListener { _, value, _ ->
            binding.tvWarmthLabel.text = "Warmth Level: ${value.toInt()}"
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnTakePhoto.setOnClickListener {
            Log.d(TAG, "Take Photo tapped")
            checkCameraAndLaunch()
        }

        binding.btnGallery.setOnClickListener {
            Log.d(TAG, "Gallery tapped")
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveItem()
        }
    }

    private fun checkCameraAndLaunch() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            launchCamera()
        } else {
            Log.d(TAG, "Requesting camera permission")
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File.createTempFile(
                "shela_${System.currentTimeMillis()}_",
                ".jpg",
                requireContext().cacheDir
            )
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            pendingCameraUri = uri
            Log.d(TAG, "Launching camera with output $uri")
            takePicture.launch(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch camera", e)
            Toast.makeText(requireContext(), "Could not open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.pbProcessing.visibility = View.VISIBLE

            val base64 = withContext(Dispatchers.IO) {
                ImageUtils.uriToCompressedBase64(requireContext(), uri)
            }

            binding.pbProcessing.visibility = View.GONE

            if (base64 != null) {
                pendingImageBase64 = base64
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    ImageUtils.base64ToBitmap(base64)
                }
                binding.ivPreview.setImageBitmap(bitmap)
                binding.ivPreview.visibility = View.VISIBLE
                binding.llPhotoPrompt.visibility = View.GONE
                Log.i(TAG, "Image ready for save (${base64.length / 1024} KB)")
            } else {
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveItem() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val category = binding.actCategory.text?.toString()?.trim().orEmpty()
        val brand = binding.etBrand.text?.toString()?.trim().orEmpty()
        val priceStr = binding.etPrice.text?.toString()?.trim().orEmpty()
        val season = binding.actSeason.text?.toString()?.trim().orEmpty().ifBlank { "All-Season" }
        val warmth = binding.sliderWarmth.value.toInt()
        val waterResistant = binding.switchWaterResistant.isChecked

        // Clear previous errors
        binding.tilName.error = null
        binding.tilCategory.error = null

        if (name.isBlank()) {
            binding.tilName.error = "Please enter an item name"
            return
        }
        if (category.isBlank()) {
            binding.tilCategory.error = "Please select a category"
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0

        val item = ClothingItem(
            name = name,
            category = category,
            brand = brand,
            purchasePrice = price,
            imageBase64 = pendingImageBase64.orEmpty(),
            season = season,
            warmthLevel = warmth,
            isWaterResistant = waterResistant
        )

        Log.d(TAG, "Saving item: $item")
        viewModel.saveItem(item)
        Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}