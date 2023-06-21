package com.example.firesense_app

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils
import org.json.JSONArray
import org.json.JSONObject

class ProgressActivity : AppCompatActivity() {

    var isStarted = false
    var progressStatus = 0
    var handler: Handler? = null
    var secondaryHandler: Handler? = Handler()
    var primaryProgressStatus = 0
    var secondaryProgressStatus = 0

    var activityTo: String = ""
    var activityFrom: String = ""

    private lateinit var mission: Mission
    private lateinit var fire: Fire
    val util = Utils(this)

    // lateinit  var images: JSONArray
    var comm = Communication()

    //  val images: MutableList<String> = ArrayList()
    var imagesMosaic = ArrayList<String>()
    var imagesRgb = ArrayList<String>()
    var imagesThermal = ArrayList<String>()
    var firesJetson = ArrayList<Fire>()
    var connect: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        activityTo = intent.getSerializableExtra("activityTo") as String
        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        activityFrom = intent.getSerializableExtra("activityFrom") as String
        // var opt =""
        when (activityTo) {
            "mission" -> {
                mission = intent.getSerializableExtra("mission") as Mission
                fire = intent.getSerializableExtra("fire") as Fire
                startMission()
            }
            "settings" -> {
                startSettings()
            }
            else -> {
                /*  print("x no es 1 o 2") */
            }
        }
    }

    fun startSettings() {
        var jsonObject = JSONObject()
        AsyncTask.execute {
            jsonObject = comm.getInfo()
            // util.createFile("jetsonSetting.json",jsonObject.toString())
        }
        Handler()
                .postDelayed(
                        {
                            // start main activity
                            val intent = Intent(this@ProgressActivity, InfoActivity::class.java)
                            intent.putExtra("firesJetson", firesJetson)
                            intent.putExtra("info", jsonObject.toString())
                            startActivity(intent)
                            finish()
                        },
                        3000
                )
    }

    fun startMission() {
        var mosaic: JSONArray? = null
        var rgb: JSONArray? = null
        var thermal: JSONArray? = null

        AsyncTask.execute {
            var client = comm.getClientConnection()
            // si no hay comunicacion con jetson
            if (client == null) {
                util.showToast("Sin conexión a FireSense")
                imagesMosaic = util.readLocalImg(fire.name, mission.name, "MOSAICO")
                imagesRgb = util.readLocalImg(fire.name, mission.name, "RGB")
                imagesThermal = util.readLocalImg(fire.name, mission.name, "THERMAL")
                // connect = false
            } else {
                client.close()
                mosaic = comm.getJsonImages(fire.name, mission.name, "MOSAICO")
                rgb = comm.getJsonImages(fire.name, mission.name, "RGB")
                thermal = comm.getJsonImages(fire.name, mission.name, "THERMAL")
                for (i in 0 until mosaic!!.length()) {
                    var str = mosaic!!.getString(i)
                    if (!str.contains("txt", ignoreCase = true)) imagesMosaic.add(str)
                }
                for (i in 0 until rgb!!.length()) {
                    imagesRgb.add(rgb!!.getString(i))
                }
                for (i in 0 until thermal!!.length()) {
                    imagesThermal.add(thermal!!.getString(i))
                }
                connect = true
            }
        }
        Handler()
                .postDelayed(
                        {
                            // start main activity
                            val intent = Intent(this@ProgressActivity, MissionActivity::class.java)
                            intent.putExtra("mission", mission)
                            intent.putExtra("fire", fire)
                            intent.putExtra("imagesMosaic", imagesMosaic)
                            intent.putExtra("imagesRgb", imagesRgb)
                            intent.putExtra("imagesThermal", imagesThermal)
                            intent.putExtra("firesJetson", firesJetson)
                            intent.putExtra("connect", connect)
                            intent.putExtra("activityFrom", activityFrom)
                            startActivity(intent)
                            finish()
                        },
                        4000
                )
    }
}
