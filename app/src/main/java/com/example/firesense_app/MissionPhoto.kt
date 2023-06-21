package com.example.firesense_app

import android.os.Parcelable
import android.util.Log
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Utils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MissionPhoto(val url: String?) : Parcelable {
    var comm = Communication()
    companion object {
        fun getSunsetPhotos(
                images: ArrayList<String>,
                folder: String,
                mission: String,
                fire: String,
                utils: Utils,
                comm: Communication,
                connect: Boolean
        ): Array<MissionPhoto> {
            Log.i("TAMAÑO", images.size.toString())
            val imagenes: ArrayList<MissionPhoto> = ArrayList<MissionPhoto>()
            for (i in 0 until images.size) {
                var url: String = ""
                if (connect) {
                    url =
                            ("http://" +
                                            comm.ipJetson +
                                            "/FIRES/" +
                                            fire +
                                            "/" +
                                            mission +
                                            "/" +
                                            folder +
                                            "/" +
                                            images.get(i))
                                    .replace(" ", "%20")
                } else {
                    url =
                            ("/storage/emulated/0/Android/data/com.example.firesense_app/FIRES/" +
                                    fire +
                                    "/" +
                                    mission +
                                    "/" +
                                    folder +
                                    "/" +
                                    images.get(i))
                }
                imagenes.add(MissionPhoto(url))
                Log.i("arrayqlo", url)
            }
            return imagenes.toTypedArray()
            /*   return arrayOf<MissionPhoto>(
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"),
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"),
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"),
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"),
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"),
            MissionPhoto("http://192.168.1.43/FIRES/incendio%201/mision%201/RGB/205.jpg"))*/
        }
    }
}
