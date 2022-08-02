package com.example.gittutorial

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.ContentObservable
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gittutorial.PaginationScrollListener.Companion.PAGE_SIZE
import com.example.gittutorial.PaginationScrollListener.Companion.PAGE_START
import com.example.gittutorial.databinding.ActivityScopeStorageBinding
import com.plcoding.androidstorage.InternalStoragePhoto
import com.plcoding.androidstorage.SharedStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.IOException
import java.util.*

class ScopeStorageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScopeStorageBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var externalStoragePhotoAdapter: SharedPhotoAdapter
    private var takePhoto: ActivityResultLauncher<Void?>? = null
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var contentObserver: ContentObserver
    private var deletedImageUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScopeStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            lifecycleScope.launch {
                val isDeletionSuccessful = deletePhotoFromInternalStorage(it.name)
                if (isDeletionSuccessful) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(this@ScopeStorageActivity , "Photo successfully deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ScopeStorageActivity, "Failed to delete photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        externalStoragePhotoAdapter = SharedPhotoAdapter {
        lifecycleScope.launch {
            deleteImageFromSharedStorage(it.contentUri)
            deletedImageUri = it.contentUri
        }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if(readPermissionGranted) {
                lifecycleScope.launch(Dispatchers.IO) {
                    loadPhotosFromExternalStorageIntoRecyclerView()
                }
            } else {
                Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG).show()
            }
        }
        updateOrRequestPermissions()

        intentSenderLauncher =  registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (isSDK29()) {
                    lifecycleScope.launch {
                        deleteImageFromSharedStorage(deletedImageUri ?: return@launch)
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
           loadPhotosFromExternalStorageIntoRecyclerView()
        }

        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            lifecycleScope.launch {
                val isPrivate = binding.switchPrivate.isChecked
                val isSavedSuccessfully = when {
                    isPrivate -> savePhotoToInternalStorage(UUID.randomUUID().toString(), it!!)
                    writePermissionGranted -> savePhotoToExternalStorage(UUID.randomUUID().toString(), it!!)
                    else -> false
                }
                if (isPrivate) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                }else{
                    loadPhotosFromExternalStorageIntoRecyclerView()
                }
                if (isSavedSuccessfully) {
                    Toast.makeText(this@ScopeStorageActivity, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ScopeStorageActivity, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //setupSwipeRefreshListener()

        binding.btnTakePhoto.setOnClickListener {
            requestPermission()
        }
        initContentObserver()

        setupInternalStorageRecyclerView()
        setupExternalStorageRecyclerView()

        loadPhotosFromInternalStorageIntoRecyclerView()

            loadPhotosFromExternalStorageIntoRecyclerView()



    }
//    private fun setupSwipeRefreshListener() {
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            currentPage = PAGE_START
//            currentItemCount = currentPage
//            isLastPage = false
//            isLoading = false
//            sharedPhotosList = emptyList<SharedStoragePhoto>().toMutableList()
//            binding.swipeRefreshLayout.isRefreshing = false
//            loadPhotosFromExternalStorageIntoRecyclerView()
//        }
//    }

    private suspend fun deleteImageFromSharedStorage(contentUri: Uri) {
        return withContext(Dispatchers.IO) {
            try {
                // case 1 : delete image on api level 29 below
                contentResolver.delete(contentUri, null, null)
            } catch (e: SecurityException) {
                // case 2 : delete image on api level 29 and above
                val intentSender = when {
                    isSDK29() -> {
                        val recoverableSecurityException =
                            e as? RecoverableSecurityException ?: throw RuntimeException(
                                e.message,
                                e
                            )
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    }

                    isSDK30AndUp() -> {
                        MediaStore.createDeleteRequest(
                            contentResolver,
                            listOf(contentUri)
                        ).intentSender
                    }

                    else -> null
                }

                intentSender?.let {
                    intentSenderLauncher.launch(IntentSenderRequest.Builder(it).build())
                }
                Log.e("TAG", "deleteImageFromSharedStorage: ${e.message}")
            }
        }
    }

    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if (selfChange && readPermissionGranted) {
                    loadPhotosFromExternalStorageIntoRecyclerView()
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
    private suspend fun loadPhotosFromExternalStorage(): List<SharedStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )
            val photos = mutableListOf<SharedStoragePhoto>()
            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

            )?.use { cursor ->
                val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

               // var count = currentItemCount

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumnIndex)
                    val displayName = cursor.getString(displayNameColumnIndex)
                    val width = cursor.getInt(widthColumnIndex)
                    val height = cursor.getInt(heightColumnIndex)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(SharedStoragePhoto(id, displayName, width, height, contentUri))
                }
                photos.toList()
            } ?: listOf()
        }
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
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())

        }
    }


    private suspend fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            val imageCollection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
            try {
                contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    contentResolver.openOutputStream(uri).use { outputStream ->
                        if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                            throw IOException("Couldn't save bitmap")
                        }
                    }
                } ?: throw IOException("Couldn't create MediaStore entry")
                true
            } catch(e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    private fun setupExternalStorageRecyclerView() = binding.rvPublicPhotos.apply {
        adapter = externalStoragePhotoAdapter
        layoutManager =  StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)

//        this.addOnScrollListener(object :
//            PaginationScrollListener(layoutManager) {
//            override fun loadMoreItems() {
//                currentItemCount = currentPage++ * PAGE_SIZE
//                loadPhotosFromExternalStorageIntoRecyclerView()
//            }
//
//            override fun isLoading() = isLoading
//
//            override fun isLastPage() = isLastPage
//        })
    }


    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun loadPhotosFromExternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
                val pictures = withContext(Dispatchers.IO) {
                    loadPhotosFromExternalStorage()
                }
                externalStoragePhotoAdapter.submitList(pictures)
                Log.i("TAG", "loadDataIntoSharedStorageAdapter: ${pictures.size}")

        }
    }

    private suspend fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }
    }

    private suspend fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                    if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
                true
            } catch(e: IOException) {
                e.printStackTrace()
                false
            }
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

}