package com.example.gittutorial

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gittutorial.databinding.ActivityScopeStorageBinding
import com.plcoding.androidstorage.InternalStoragePhoto
import com.plcoding.androidstorage.SharedStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ScopeStorageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScopeStorageBinding
    private val internalStoragePhotoAdapter by lazy {
        InternalStoragePhotoAdapter (
            onPhotoClick =  ::longClickDeleteInternalPhoto
        )
    }
    private val externalStoragePhotoAdapter by lazy {
        SharedPhotoAdapter (
            onPhotoClick =  ::longClickDeleteExternalPhoto
        )
    }
    private var takePhoto: ActivityResultLauncher<Void?>? = null
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var contentObserver: ContentObserver
    private var deletedImageUri: Uri? = null
    private val viewModel: ScopedStorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScopeStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeObserver()
        setupSwipeRefreshListener()
        setupPermissionLauncher()
        updateOrRequestPermissions()
        setupIntentSenderLauncher()
        setupTakePhotoPreviewLauncher()
        binding.btnTakePhoto.setOnClickListener {
            requestPermission()
        }
        initContentObserver()
        setupInternalStorageRecyclerView()
        setupExternalStorageRecyclerView()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.loadPhotosFromExternalStorage(this@ScopeStorageActivity)
            viewModel.loadPhotosFromInternalStorage(this@ScopeStorageActivity)
        }
    }

    private fun setupTakePhotoPreviewLauncher() {
        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            lifecycleScope.launch {
                val isPrivate = binding.switchPrivate.isChecked
                when {
                    isPrivate -> it?.let {
                        viewModel.savePhotoToInternalStorage(
                            UUID.randomUUID().toString(), it, this@ScopeStorageActivity
                        )
                    }
                    writePermissionGranted -> it?.let {
                        viewModel.savePhotoToExternalStorage(
                            UUID.randomUUID().toString(), it, this@ScopeStorageActivity
                        )
                    }
                }
                if (isPrivate) {
                    viewModel.loadPhotosFromInternalStorage(this@ScopeStorageActivity)
                } else {
                    viewModel.loadPhotosFromExternalStorage(this@ScopeStorageActivity)
                }

            }
        }
    }

    private fun setupIntentSenderLauncher() {
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    if (isSDK29()) {
                        lifecycleScope.launch {
                            viewModel.deleteImageFromSharedStorage(
                                deletedImageUri ?: return@launch,
                                intentSenderLauncher,
                                this@ScopeStorageActivity
                            )
                        }
                    }
                    Toast.makeText(
                        this@ScopeStorageActivity,
                        "Image deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ScopeStorageActivity,
                        "Failed to delete image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.loadPhotosFromExternalStorage(this)
            }
    }

    private fun setupPermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted

                if (readPermissionGranted) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.loadPhotosFromExternalStorage(this@ScopeStorageActivity)
                    }
                } else {
                    Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun setupSwipeRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.loadPhotosFromExternalStorage(this@ScopeStorageActivity)
        }
    }

    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if (selfChange && readPermissionGranted) {
                    viewModel.loadPhotosFromExternalStorage(this@ScopeStorageActivity)
                }
            }
        }
        // register to content observer for listening any changes made at image collection on shared storage.
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    private fun setupExternalStorageRecyclerView() = binding.rvPublicPhotos.apply {
        adapter = externalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    private fun subscribeObserver() {
        viewModel.sharedData.observe(this, androidx.lifecycle.Observer {
            loadPhotosFromExternalStorageIntoRecyclerView(it)
        })
        viewModel.privateData.observe(this, androidx.lifecycle.Observer {
            loadPhotosFromInternalStorageIntoRecyclerView(it)
        })
        viewModel.isPrivatePhotoDeleted.observe(this, androidx.lifecycle.Observer {
            checkIfInternalPhotoIsDeleted(it)
        })
        viewModel.isPrivatePhotoSaved.observe(this, androidx.lifecycle.Observer {
            checkIfPhotoSaved(it)
        })
        viewModel.isSharedPhotoSaved.observe(this, androidx.lifecycle.Observer {
            checkIfPhotoSaved(it)
        })
    }

    private fun checkIfPhotoSaved(isPhotoSaved: Boolean) {
        if (isPhotoSaved) {
            Toast.makeText(
                this@ScopeStorageActivity,
                "Photo saved successfully",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this@ScopeStorageActivity, "Failed to save photo", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkIfInternalPhotoIsDeleted(isDeleted: Boolean) {
        lifecycleScope.launch {
            if (isDeleted) {
                viewModel.loadPhotosFromInternalStorage(this@ScopeStorageActivity)
                Toast.makeText(
                    this@ScopeStorageActivity,
                    "Photo successfully deleted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@ScopeStorageActivity,
                    "Failed to delete photo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadPhotosFromInternalStorageIntoRecyclerView(listOfPrivateData: List<InternalStoragePhoto>) {
        lifecycleScope.launch {
            internalStoragePhotoAdapter.submitList(listOfPrivateData)
        }
    }

    private fun loadPhotosFromExternalStorageIntoRecyclerView(listOfSharedData: List<SharedStoragePhoto>) {
        lifecycleScope.launch {
            externalStoragePhotoAdapter.submitList(listOfSharedData)
            Log.i("TAG", "loadDataIntoSharedStorageAdapter: ${listOfSharedData.size}")
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            takePhoto?.let { it.launch() }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhoto?.let { it.launch() }
        } else {
            Toast.makeText(this, "permission is not granted ", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }

    private fun longClickDeleteInternalPhoto(internalStoragePhoto: InternalStoragePhoto){
        viewModel.deletePhotoFromInternalStorage(internalStoragePhoto.name, this@ScopeStorageActivity)
    }
    private fun longClickDeleteExternalPhoto(sharedStoragePhoto : SharedStoragePhoto){
        viewModel.deleteImageFromSharedStorage(
            sharedStoragePhoto.contentUri,
            intentSenderLauncher,
            this@ScopeStorageActivity
        )
        deletedImageUri = sharedStoragePhoto.contentUri
    }
}