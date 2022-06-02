package com.example.sampleimgencrypapp

import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.example.sampleimgencrypapp.Utils.MyEncrypter
import java.io.*


class MainActivity : AppCompatActivity() {

    lateinit var myDir:File
    lateinit var imageView:ImageView

    companion object {
        private val FILE_NAME_ENC = "test_enc"
        private val FILE_NAME_DEC = "test_dec.jpg"

        private val key="poOkWuKk9Be1Hq01" //16 char = 128 bit random unique key
        private val specString = "H3yT5Cx1FDw1Uci0" //16 char = 128 bit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_enc = findViewById(R.id.btn_enc) as Button
        val btn_dec = findViewById(R.id.btn_dec) as Button
        Dexter.withActivity(this)
            .withPermissions(*arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    btn_enc.isEnabled = true
                    btn_dec.isEnabled = true
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Toast.makeText(this@MainActivity, "You should accept permission", Toast.LENGTH_SHORT).show()
                }
            })
            .check()

        val root = getExternalFilesDir(Environment.getRootDirectory().toString())
        myDir = File("$root/saved_images")
        if(!myDir.exists())
            myDir.mkdirs()

        btn_enc.setOnClickListener {
            //Converting drawable to bitmap
            val drawable = ContextCompat.getDrawable(this@MainActivity,R.drawable.test)
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            val input= ByteArrayInputStream(stream.toByteArray())
            val outputFileEnc = File(myDir, FILE_NAME_ENC) // Create empty file enc


            try {
                MyEncrypter.encryptToFile(key, specString, input, FileOutputStream(outputFileEnc))
                Toast.makeText(this@MainActivity, "Encrypted", Toast.LENGTH_SHORT).show()
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }        }



        btn_dec.setOnClickListener {
            val outputFileDec = File(myDir, FILE_NAME_DEC) //Creates empty file dec
            val encFile = File(myDir, FILE_NAME_ENC)
            try {
                MyEncrypter.decryptToFile(key, specString, FileInputStream(encFile), FileOutputStream(outputFileDec))

                //Set for ImageView
                imageView = findViewById(R.id.imageView)
                imageView.setImageURI(Uri.fromFile(outputFileDec))

                //To delete decrypted drawable use
                //outputFileDec.delete()

                Toast.makeText(this@MainActivity, "Decrypted", Toast.LENGTH_SHORT).show()

            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }

        }
    }
}