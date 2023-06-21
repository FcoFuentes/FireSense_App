package com.example.firesense_app

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.firesenseapp.Utils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MissionPhotoActivity : AppCompatActivity() {
    val util = Utils(this)
    companion object {
        const val EXTRA_SUNSET_PHOTO = "SunsetPhotoActivity.EXTRA_SUNSET_PHOTO"
    }

    var connect: Boolean = true
    private lateinit var imageView: ImageView
    private lateinit var missionPhoto: MissionPhoto
    private lateinit var missionName: String
    private lateinit var fireName: String
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_photo)
        connect = intent.getSerializableExtra("connect") as Boolean

        missionPhoto = intent.getParcelableExtra(EXTRA_SUNSET_PHOTO)
        missionName = intent.getSerializableExtra("missionName") as String
        fireName = intent.getSerializableExtra("fireName") as String
        imageView = findViewById(R.id.image)
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    override fun onStart() {
        super.onStart()
        Log.i("onBindViewHolderonStart", missionPhoto.url)
        var ruta = ""
        if (connect) ruta = missionPhoto.url.toString() else ruta = "file://" + missionPhoto.url
        Picasso.get()
                .load(ruta)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .fit()
                .into(
                        imageView,
                        object : Callback {

                            override fun onSuccess() {
                                bitmap = (imageView.drawable as BitmapDrawable).bitmap
                                onPalette(Palette.from(bitmap!!).generate())
                            }
                            override fun onError(e: Exception?) {}
                        }
                )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // util.showToast("onCreateOptionsMenu")

        menuInflater.inflate(R.menu.menu_image_view, menu)

        // menu_ = menu
        // menu.findItem(R.id.connectIcon).setIcon(R.drawable.noconnect);
        // menu_ = menu
        //      mOptionsMenu = menu;

        /*AsyncTask.execute {
            var client = comm.getClientConnection()
            //si no hay comunicacion con jetson
            if(client == null) {
                util.showToast("noconnect menu")
                menu_.findItem(R.id.connectIcon).setIcon(R.drawable.noconnect);
            }
            else {
                util.showToast("connect menu")
                menu_.findItem(R.id.connectIcon).setIcon(R.drawable.connect);
            }
        }*/

        /*   runOnUiThread {
            var client = comm.getClientConnection()
            //si no hay comunicacion con jetson
            if(client == null) {
                util.showToast("noconnect")
                menu.findItem(R.id.connectIcon).setIcon(R.drawable.noconnect);
            }
            else {
                util.showToast("connect")
                menu.findItem(R.id.connectIcon).setIcon(R.drawable.connect);
            }
        }*/
        return true
    }
    fun getBitmapFromView(bmp: Bitmap?): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(this.externalCacheDir, System.currentTimeMillis().toString() + ".png")
            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_share -> {
                if (util.isImported(this.missionName, this.fireName)) {
                    val gf =
                            BitmapFactory.decodeFile(
                                    "/storage/emulated/0/Android/data/com.example.firesense_app/FIRES" +
                                            missionPhoto.url!!.split("FIRES").get(1)
                            )
                    val compartirgf = Intent(Intent.ACTION_SEND)
                    compartirgf.type = "image/jpeg"

                    val valoresgf = ContentValues()
                    valoresgf.put(MediaStore.Images.Media.TITLE, "imagen")
                    valoresgf.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    val urigf =
                            contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    valoresgf
                            )
                    val outstreamgf: OutputStream?
                    try {
                        outstreamgf = contentResolver.openOutputStream(urigf!!)
                        gf.compress(Bitmap.CompressFormat.JPEG, 100, outstreamgf)
                        outstreamgf!!.close()
                    } catch (e: Exception) {
                        System.err.println(e.toString())
                    }
                    compartirgf.putExtra(Intent.EXTRA_STREAM, urigf)
                    startActivity(Intent.createChooser(compartirgf, "Compartir imagen"))
                } else util.showToast("Misión no importada")
            }
            R.id.action_usb -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onPalette(palette: Palette?) {
        if (null != palette) {
            val parent = imageView.parent.parent as ViewGroup
            parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY))
        }
    }
}
