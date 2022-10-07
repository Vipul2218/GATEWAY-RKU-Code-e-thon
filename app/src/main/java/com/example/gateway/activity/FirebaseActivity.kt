package com.example.gateway.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.gateway.R
import com.example.gateway.models.LikeUnlikeData
import com.example.gateway.utils.ActivityManagePermission
import com.example.gateway.utils.PermissionResult
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_firebase.*
import java.io.*


class FirebaseActivity : ActivityManagePermission() {

    private var progressDialog: ProgressDialog? = null
    private val requestCodeForVideoPick = 1
    private var selectedImageUri: Uri? = null
    private var path: String? = ""
    private var firebaseVideoUrl: String? = ""
    private var file: File? = null
    private var isLikeOrUnlike = false
    private var unLikeCount = 0
    val referenceLikeUnlike = FirebaseDatabase.getInstance().getReference("LikeUnlike")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase)
        actFirebaseFabSelectVideo.setOnClickListener {
            progressDialog = ProgressDialog(this)
            progressDialog!!.setCancelable(false)
            galleryIntent()
        }
        actFirebaseIvVideo.setOnClickListener {
            if (!firebaseVideoUrl.isNullOrEmpty()) {
                openDefaultVideoPlayer(this@FirebaseActivity, firebaseVideoUrl)
            }
        }
        actFirebaseClLike.setOnClickListener {
            isLikeOrUnlike = !isLikeOrUnlike
            likeUnlikeUpdateValue(isLikeOrUnlike)
        }
//        actFirebaseClUnLike.setOnClickListener {
//            likeUnlikeUpdateValue()
//        }
        readDataFromFirebase()
        updateLikeUnlikeUi()
    }

    private fun galleryIntent() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
            startActivityForResult(
                Intent.createChooser(photoPickerIntent, getString(R.string.str_select_video)),
                requestCodeForVideoPick
            )
        } else {
            val permissionAsk = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            askCompactPermissions(permissionAsk, object : PermissionResult {
                override fun permissionGranted() {
                    galleryIntent()
                }

                override fun permissionDenied() {
                    galleryIntent()
                }

                override fun permissionForeverDenied() {
//                    showAlert(
//                        resources.getString(R.string.permission_needed_of_media_storage),
//                        resources.getString(R.string.dialog_cancel)
//                    )
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == requestCodeForVideoPick && resultCode == RESULT_OK) {
                if (data != null) {
                    selectedImageUri = data.data
                    selectedFileFromGalleryMethod(selectedImageUri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectedFileFromGalleryMethod(selectedImageUri: Uri?) {
        if (null != selectedImageUri) {
            if (selectedImageUri.toString()
                    .startsWith("content://com.google.android.apps.photos.content")
            ) {
                try {
                    path = getPathFromInputStreamUri(this, selectedImageUri)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                path = getPathFromURI(selectedImageUri)?.trim()
            }
            Log.e("VIDEO", "SelectedFileFromGallery Path : $path")
            if (path != null) {
                try {
                    file = File(path)
                    Log.e("Video file: ", file!!.absolutePath + "")
                    uploadVideo()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getPathFromInputStreamUri(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        var filePath: String? = null
        if (uri.authority != null) {
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val photoFile: File = createTemporalFileFrom(context, inputStream)!!
                filePath = photoFile.path
            } catch (e: FileNotFoundException) {
                Log.e("FileNotFoundException: ", e.toString() + "")
            } catch (e: IOException) {
                Log.e("FileNotFoundException: ", e.toString() + "")
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return filePath
    }

    @Throws(IOException::class)
    private fun createTemporalFileFrom(context: Context, inputStream: InputStream?): File? {
        var targetFile: File? = null
        if (inputStream != null) {
            var read: Int
            val buffer = ByteArray(8 * 1024)
            targetFile = setImageUri(context)
            val outputStream: OutputStream = FileOutputStream(targetFile)
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return targetFile
    }

    private fun setImageUri(context: Context?): File? {
        val file: File
        val cw = ContextWrapper(context)
        val directory = cw.getDir("imageDir", MODE_PRIVATE)
        file = File(directory, System.currentTimeMillis().toString() + ".mp4")
        return file
    }

    private fun getPathFromURI(selectedImageUri: Uri): String? {
        var res: String? = null
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor =
                contentResolver.query(selectedImageUri, projection, null, null, null)!!
            if (cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                res = cursor.getString(columnIndex)
            }
            cursor.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return res
    }

    private fun uploadVideo() {
        progressDialog!!.show()
        if (selectedImageUri != null) {
            // save the selected video in Firebase storage
            val reference = FirebaseStorage.getInstance()
                .getReference(
                    "VideoFiles/" + System.currentTimeMillis() + "." + getFileType(
                        selectedImageUri!!
                    )
                )
            reference.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                // get the link of video
                val downloadUri: String = uriTask.result.toString()
                val reference1 = FirebaseDatabase.getInstance().getReference("VideoList")
                val map: HashMap<String, String> = HashMap()
                map["videolink"] = downloadUri
                reference1.child("" + System.currentTimeMillis()).setValue(map)
                // Video uploaded successfully
                // Dismiss dialog
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@FirebaseActivity,
                    getString(R.string.str_video_upload_success),
                    Toast.LENGTH_SHORT
                ).show()
                firebaseVideoUrl = downloadUri
                println("firebaseVideoUrl=$firebaseVideoUrl")
                displayVideo()
            }.addOnFailureListener { e -> // Error, Image not uploaded
                progressDialog!!.dismiss()
                println("VideoFail=${e.message}")
                Toast.makeText(
                    this@FirebaseActivity,
                    getString(R.string.str_video_upload_fail),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }.addOnProgressListener { taskSnapshot ->
                // Progress Listener for loading
                // percentage on the dialog box
                // show the progress bar
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog!!.setMessage("Uploaded " + progress.toInt() + "%")
            }
        }
    }

    private fun getFileType(videoUri: Uri): String? {
        val r = contentResolver
        // get the file type ,in this case its mp4
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videoUri))
    }

    private fun openDefaultVideoPlayer(callingActivity: Activity?, videoUrl: String?) {
//        val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(videoUrl), "video/mp4")
        try {
            callingActivity!!.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            callingActivity!!.startActivity(intent)
        }
    }

    private fun displayVideo() {
        Glide
            .with(this@FirebaseActivity)
            .load(firebaseVideoUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_default_video_image)
            .into(actFirebaseIvVideo)
        actFirebaseCvVideo.visibility = View.VISIBLE
    }

    private fun likeUnlikeUpdateValue(isLike: Boolean) {
        val likeUnlikeData: HashMap<String, Boolean> = HashMap()
        likeUnlikeData["isLike"] = isLike
        referenceLikeUnlike.child("").setValue(likeUnlikeData).addOnSuccessListener {
            // Write was successful!
            // ...
        }.addOnFailureListener {
            it.message
        }

    }

    private fun readDataFromFirebase() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val readData = dataSnapshot.value.toString()
                if (readData != null) {
                    println("Read Value=${readData}")
                    val likeUnlikeData = Gson().fromJson(readData, LikeUnlikeData::class.java)
                    if (likeUnlikeData != null) {
                        isLikeOrUnlike = likeUnlikeData.isLike!!
                        println("Read Value=${isLikeOrUnlike}")
                        updateLikeUnlikeUi()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Fail Read Value=${databaseError.toException()}")
            }
        }
        referenceLikeUnlike.addValueEventListener(postListener)
    }

    private fun updateLikeUnlikeUi() {
        if (isLikeOrUnlike) {
            actFirebaseIvLike.text = getString(R.string.str_unlike)
            actFirebaseTvLike.text = getString(R.string.str_like_count)
            actFirebaseIvLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_down, 0, 0, 0)
        } else {
            actFirebaseIvLike.text = getString(R.string.str_like)
            actFirebaseTvLike.text = getString(R.string.str_unlike_count)
            actFirebaseIvLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up, 0, 0, 0)
        }
    }
}
