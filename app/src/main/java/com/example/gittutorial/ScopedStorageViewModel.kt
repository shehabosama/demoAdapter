package com.example.gittutorial

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.androidstorage.InternalStoragePhoto
import com.plcoding.androidstorage.SharedStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ScopedStorageViewModel: ViewModel() {
    private val _sharedData = MutableLiveData<List<SharedStoragePhoto>>(listOf())
    val sharedData : LiveData<List<SharedStoragePhoto>>
    get() = _sharedData

    private val _privateData = MutableLiveData<List<InternalStoragePhoto>>()
    val privateData:LiveData<List<InternalStoragePhoto>>
    get() = _privateData

    private val _isPrivatePhotoDeleted = MutableLiveData<Boolean>()
    val isPrivatePhotoDeleted:LiveData<Boolean>
    get()=_isPrivatePhotoDeleted

    private val _isPrivatePhotoSaved = MutableLiveData<Boolean>()
    val isPrivatePhotoSaved :LiveData<Boolean>
    get()=_isPrivatePhotoSaved

    private val _isSharedPhotoSaved = MutableLiveData<Boolean>()
    val isSharedPhotoSaved :LiveData<Boolean>
    get()=_isSharedPhotoSaved


      fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap,context:Context) {
        viewModelScope.launch {
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
                context.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    context.contentResolver.openOutputStream(uri).use { outputStream ->
                        if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                            throw IOException("Couldn't save bitmap")
                        }
                    }
                } ?: throw IOException("Couldn't create MediaStore entry")
               _isSharedPhotoSaved.value = true
            } catch(e: IOException) {
                e.printStackTrace()
                _isSharedPhotoSaved.value = false
            }
        }
    }

      fun savePhotoToInternalStorage(filename: String, bmp: Bitmap,context:Context) {
       viewModelScope.launch{
            try {
                context.openFileOutput("$filename.jpg", AppCompatActivity.MODE_PRIVATE).use { stream ->
                    if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
                _isPrivatePhotoSaved.value = true
            } catch(e: IOException) {
                e.printStackTrace()
                _isPrivatePhotoSaved.value = false
            }
        }
    }

     suspend fun loadPhotosFromInternalStorage(context:Context) {
         viewModelScope.launch {
             _privateData.value = withContext(Dispatchers.IO){
                 val files = context.filesDir.listFiles()
                 files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                     val bytes = it.readBytes()
                     val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                     InternalStoragePhoto(it.name, bmp)
                 } ?: listOf()
             }

         }
    }

     fun loadPhotosFromExternalStorage(context:Context){
         viewModelScope.launch{
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
            context.contentResolver.query(
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
                _sharedData.value = photos
            }
        }
    }

      fun deleteImageFromSharedStorage(contentUri: Uri,intentSenderLauncher:ActivityResultLauncher<IntentSenderRequest> , context: Context) {
       viewModelScope.launch {
            try {
                // case 1 : delete image on api level 29 below
                context.contentResolver.delete(contentUri, null, null)
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
                            context.contentResolver,
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
    fun deletePhotoFromInternalStorage(filename: String , context:Context) {
       viewModelScope.launch {
            try {
                _isPrivatePhotoDeleted.value= context.deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                _isPrivatePhotoDeleted.value= false
            }
        }
    }

}