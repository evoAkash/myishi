package com.example.ishi

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class HomeScreen : AppCompatActivity(),View.OnClickListener {
    private val TAG = "StorageActivity"
    //track Choosing Image Intent
    private val CHOOSING_IMAGE_REQUEST = 1234

    private var fileUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var imageReference: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        tvFileName.text = ""

        imageReference = FirebaseStorage.getInstance().reference.child("images")

        btn_choose_file.setOnClickListener(this)
        btn_upload_byte.setOnClickListener(this)
    }
    override fun onClick(view: View?) {
        val i = view!!.id

        when (i) {
            R.id.btn_choose_file -> showChoosingFile()
            R.id.btn_upload_byte -> uploadBytes()
            R.id.btn_back -> finish()
        }}
        private fun uploadBytes() {
            if (fileUri != null) {
                val fileName = edtFileName.text.toString()

                if (!validateInputFileName(fileName)) {
                    return
                }

                val baos = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data: ByteArray = baos.toByteArray()

                val fileRef = imageReference!!.child(fileName + "." + getFileExtension(fileUri!!))
                fileRef.putBytes(data)
                    .addOnSuccessListener { taskSnapshot ->
                        Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                        tvFileName.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                        Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        // progress percentage
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                        // percentage in progress dialog
                        val intProgress = progress.toInt()
                        tvFileName.text = "Uploaded " + intProgress + "%..."
                    }
                    .addOnPausedListener { System.out.println("Upload is paused!") }

            } else {
                Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
            }
        }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (bitmap != null) {
            bitmap!!.recycle()
        }

        if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            fileUri = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                imgFile.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showChoosingFile() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()

        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun validateInputFileName(fileName: String): Boolean {
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}
