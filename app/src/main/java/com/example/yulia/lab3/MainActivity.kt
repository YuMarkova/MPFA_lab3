package com.example.yulia.myapplication


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import com.example.yulia.lab3.PhotoViewActivity
import com.example.yulia.lab3.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAKE_PHOTO_REQUEST  = 1
    private var m_photoName         = ""
    private var m_photoUri          = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.button_take_photo.setOnClickListener {
            val photoName = this.name_edit.text.toString()

            if (photoName.isEmpty())
                Snackbar.make(this.warning_view, R.string.storagePermissionDenied, Snackbar.LENGTH_LONG).show()
            else {
                m_photoName = photoName
                validatePermissions()
            }
        }
    }

    private fun validatePermissions() {
        val warning_view = this.warning_view

        Dexter.withActivity(this@MainActivity)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        takePhoto()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Snackbar.make(warning_view, R.string.storagePermissionDenied, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        if (token != null) token.continuePermissionRequest()
                    }
                }).check()
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val image      = File.createTempFile(m_photoName + '_' + timeStamp, ".jpg", storageDir)
        m_photoUri     = image.absolutePath

        return image
    }

    private fun takePhoto() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePhotoIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null

            try {
                photoFile = createImageFile()
            }
            catch (exc: IOException) {
                Snackbar.make(this.warning_view, exc.toString(), Snackbar.LENGTH_LONG).show()
            }

            if (photoFile != null) {
                val photoUri = Uri.fromFile(photoFile)
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_REQUEST)
            openImageViewActivity()
        else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openImageViewActivity() {
        val intent = Intent(this@MainActivity, PhotoViewActivity::class.java).apply {
            putExtra("photoUri",  m_photoUri)
            putExtra("photoName", m_photoName)
        }
        startActivity(intent)
    }

}
