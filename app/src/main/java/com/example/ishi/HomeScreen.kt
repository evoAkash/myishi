package com.example.ishi

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.item_image.view.*


class HomeScreen : AppCompatActivity(),View.OnClickListener {
    private val TAG = "StorageActivity"
    //track Choosing Image Intent
    lateinit var user: FirebaseUser
    private val CHOOSING_IMAGE_REQUEST = 1234
    private var dataReference: DatabaseReference? = null
    private var dataReference1: DatabaseReference? = null
    lateinit var rcvListImg: RecyclerView
    lateinit var rcvListImg1: RecyclerView
    private var fileUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var imageReference1: StorageReference? = null
    private var imageReference2: StorageReference? = null
    private var mAdapter: FirebaseRecyclerAdapter<UploadInfo, ImgViewHolder>? = null
    private var mAdapter1: FirebaseRecyclerAdapter<UploadInfo, ImgViewHolder>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        tvFileLoad.text = ""
        tvFileLoad.visibility = View.GONE
        user = FirebaseAuth.getInstance().getCurrentUser()!!
        dataReference = FirebaseDatabase.getInstance().getReference("images")
        dataReference1 = FirebaseDatabase.getInstance().getReference(user.phoneNumber!!)

        imageReference1 = FirebaseStorage.getInstance().reference.child("images")
        imageReference2 = FirebaseStorage.getInstance().reference.child("images/"+ user.phoneNumber )

        btn_choose_file.setOnClickListener(this)
        btn_upload_file.setOnClickListener(this)
        btn_upload_file2.setOnClickListener(this)
        val query1 = dataReference!!.limitToLast(3)
        val query = dataReference1!!.limitToLast(3)
        val layoutManager = LinearLayoutManager(this)
        val layoutManager1 = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        layoutManager1.reverseLayout = false
        rcvListImg1= findViewById(R.id.rcvListImg1)
        rcvListImg1.setHasFixedSize(true)
        rcvListImg1.layoutManager = layoutManager
        rcvListImg= findViewById(R.id.rcvListImg)
        rcvListImg.setHasFixedSize(true)
        rcvListImg.layoutManager = layoutManager1

        mAdapter = object : FirebaseRecyclerAdapter<UploadInfo, ImgViewHolder>(
            UploadInfo::class.java, R.layout.item_image, ImgViewHolder::class.java, query) {

            override fun populateViewHolder(viewHolder: ImgViewHolder?, model: UploadInfo?, position: Int) {
                viewHolder!!.itemView.tvImgName.text = model!!.name
                Picasso.with(this@HomeScreen)
                    .load(model.url)
                    .error(R.drawable.common_google_signin_btn_icon_dark)
                    .into(viewHolder.itemView.imgView)
            }
        }
        mAdapter1 = object : FirebaseRecyclerAdapter<UploadInfo, ImgViewHolder>(
            UploadInfo::class.java, R.layout.item_image, ImgViewHolder::class.java, query1) {

            override fun populateViewHolder(viewHolder: ImgViewHolder?, model: UploadInfo?, position: Int) {
                viewHolder!!.itemView.tvImgName.text = model!!.name
                Picasso.with(this@HomeScreen)
                    .load(model.url)
                    .error(R.drawable.common_google_signin_btn_icon_dark)
                    .into(viewHolder.itemView.imgView)
            }
        }

        rcvListImg.adapter = mAdapter
        rcvListImg1.adapter = mAdapter1
    }
    override fun onClick(view: View?) {
        val i = view!!.id

        when (i) {
            R.id.btn_choose_file -> showChoosingFile()
            R.id.btn_upload_file -> uploadBytes()
            R.id.btn_upload_file2 -> uploadPrivate()

        }}
        private fun uploadBytes() {
            if (fileUri != null) {
                val fileName = edtFileName.text.toString()

                if (!validateInputFileName(fileName)) {
                    return
                }
                tvFileLoad.visibility = View.VISIBLE

                val fileRef = imageReference1!!.child(fileName + "." + getFileExtension(fileUri!!))
                fileRef.putFile(fileUri!!)

                    .addOnSuccessListener { taskSnapshot ->
                        val name = taskSnapshot.metadata!!.name

                        fileRef.getDownloadUrl()
                            .addOnSuccessListener { uri ->
                                val url = uri.toString()
                                writeNewImageInfoToDB(name!!, url)
                            }
                        Log.e(TAG, "Uri: " + taskSnapshot.storage.downloadUrl)
                        Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                        tvFileLoad.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                        Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                        //writeNewImageInfoToDB(name!!, url)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        // progress percentage
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                        // percentage in progress dialog
                        val intProgress = progress.toInt()
                        tvFileLoad.text = "Uploaded " + intProgress + "%..."
                    }
                    .addOnPausedListener { System.out.println("Upload is paused!") }

            } else {
                Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
            }
        }
    private fun uploadPrivate() {
        if (fileUri != null) {
            val fileName = edtFileName.text.toString()

            if (!validateInputFileName(fileName)) {
                return
            }
            tvFileLoad.visibility = View.VISIBLE

            val fileRef = imageReference2!!.child(fileName + "." + getFileExtension(fileUri!!))
            fileRef.putFile(fileUri!!)

                .addOnSuccessListener { taskSnapshot ->
                    val name = taskSnapshot.metadata!!.name

                    fileRef.getDownloadUrl()
                        .addOnSuccessListener { uri ->
                            val url = uri.toString()
                            writeNewImageInfoToDB1(name!!, url)
                        }
                    Log.e(TAG, "Uri: " + taskSnapshot.storage.downloadUrl)
                    Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                    tvFileLoad.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                    Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                    //writeNewImageInfoToDB(name!!, url)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    // progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    // percentage in progress dialog
                    val intProgress = progress.toInt()
                    tvFileLoad.text = "Uploaded " + intProgress + "%..."
                }
                .addOnPausedListener { System.out.println("Upload is paused!") }

        } else {
            Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
        }
    }
    private fun writeNewImageInfoToDB(name: String, url: String) {
        val info = UploadInfo(name, url)

        val key = dataReference!!.push().key
        if (key != null) {
            dataReference!!.child(key).setValue(info)
        }
    }
    private fun writeNewImageInfoToDB1(name: String, url: String) {
        val info = UploadInfo(name, url)

        val key = dataReference1!!.push().key
        if (key != null) {
            dataReference1!!.child(key).setValue(info)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            fileUri = data.data

        }

    }override fun onDestroy() {
        super.onDestroy()

        mAdapter!!.cleanup()
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
