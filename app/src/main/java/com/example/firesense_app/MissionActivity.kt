package com.example.firesense_app

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils
import com.google.android.material.tabs.TabLayout
import java.io.*
import java.net.URL
import java.net.URLConnection

class MissionActivity : AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private lateinit var mission: Mission
    private lateinit var firesJetson: ArrayList<Fire>
    private var fire: Fire? = null

    val util = Utils(this)
    var comm = Communication()

    //  lateinit  var images: JSONArray
    var imagesMosaic = ArrayList<String>()
    var imagesRgb = ArrayList<String>()
    var imagesThermal = ArrayList<String>()
    var connect: Boolean = true
    var activityFrom: String = ""
    lateinit var menu_: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission)
        supportActionBar!!.title = "Misión"

        mission = intent.getSerializableExtra("mission") as Mission
        fire = intent.getSerializableExtra("fire") as Fire?
        imagesMosaic = intent.getSerializableExtra("imagesMosaic") as ArrayList<String>
        imagesRgb = intent.getSerializableExtra("imagesRgb") as ArrayList<String>
        imagesThermal = intent.getSerializableExtra("imagesThermal") as ArrayList<String>
        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        connect = intent.getSerializableExtra("connect") as Boolean
        activityFrom = intent.getSerializableExtra("activityFrom") as String

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        tabLayout.addTab(tabLayout.newTab().setText("Info"))
        tabLayout.addTab(tabLayout.newTab().setText("Mosaico"))
        tabLayout.addTab(tabLayout.newTab().setText("RGB"))
        tabLayout.addTab(tabLayout.newTab().setText("Térmica"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter =
                MyAdapter(
                        this,
                        supportFragmentManager,
                        tabLayout.tabCount,
                        mission,
                        imagesMosaic,
                        imagesRgb,
                        imagesThermal,
                        fire!!.name,
                        util,
                        comm,
                        connect
                )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewPager.currentItem = tab.position
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_mission, menu)
        menu_ = menu
        this.util.setIcon(this.menu_, this.comm, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_import1 -> {
                //   util.showToast("Importando...")
                if (mission.state.equals("FINALIZED"))
                        AsyncTask.execute {
                            var client = comm.getClientConnection()
                            if (client == null) {
                                util.showToast("Sin conexión a FireSense")
                                // firesJetson = comm.getFiresJetson(util, client)
                            } else {
                                client.close()
                                util.showToast("Iniciando...")
                                if (comm.import(fire!!.name, mission.name).equals("success")) {
                                    util.showToast("Importando...")
                                    // var url: String =
                                    // ("http://192.168.1.43/"+fire!!.name+"_"+mission.name+".zip").replace(" ","%20") 10.42.0.1
                                    var url: String =
                                            ("http://" +
                                                            comm.ipJetson +
                                                            "/" +
                                                            fire!!.name +
                                                            "_" +
                                                            mission.name +
                                                            ".zip")
                                                    .replace(" ", "%20")
                                    Log.i("action_import1", url)
                                    downloadFile(
                                            url,
                                            File(
                                                    "/storage/emulated/0/Android/data/com.example.firesense_app/mis.zip"
                                            )
                                    )
                                    util.unpackZip(fire!!.name, mission.name)
                                    util.showToast("Imagenes importadas...")
                                    mission.state = "IMPORTED"
                                    util.reWriteJson(
                                            mission.name,
                                            mission.fireName,
                                            mission.getJson(mission.fireName)
                                    )
                                } else util.showToast("error...")
                            }
                        }
                else {
                    util.showToast("Misión no finalizada")
                }
                return true
            }
            R.id.action_share -> {
                Log.i("action_import1", "action_import1")
                if (util.isImported(mission.name, mission.fireName)) {
                    util.zipAll(
                            "/storage/emulated/0/Android/data/com.example.firesense_app/FIRES/" +
                                    fire!!.name +
                                    "/" +
                                    mission.name,
                            "/storage/emulated/0/Android/data/com.example.firesense_app/" +
                                    fire!!.name +
                                    "_" +
                                    mission.name +
                                    ".zip"
                    )

                    val uri =
                            FileProvider.getUriForFile(
                                    this,
                                    BuildConfig.APPLICATION_ID + "." + localClassName + ".provider",
                                    File(
                                            "/storage/emulated/0/Android/data/com.example.firesense_app/" +
                                                    fire!!.name +
                                                    "_" +
                                                    mission.name +
                                                    ".zip"
                                    )
                            )
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.type = "application/zip"
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(sendIntent)
                    /*
                    val intentShareFile = Intent(Intent.ACTION_SEND)
                    val fileWithinMyDir = File("/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip")

                    if (fileWithinMyDir.exists()) {
                        intentShareFile.type = "application/zip"
                        intentShareFile.putExtra(
                            Intent.EXTRA_STREAM,
                            //Uri.parse("file://$myFilePath")
                            Uri.parse("/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip")
                        )
                        intentShareFile.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Sharing File..."
                        )
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
                        startActivity(Intent.createChooser(intentShareFile, "Share File"))
                    }

                     */

                    /* Log.i("action_import1","listo");
                    Log.i("action_import1 ruta","/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip")
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    Log.i("action_import1","/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip");
                    sendIntent.type = "application/zip"
                    sendIntent.putExtra(
                        Intent.EXTRA_STREAM,
                        Uri.parse("/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip")
                    )
                    Log.i("action_import compartir","/storage/emulated/0/Android/data/com.example.firesense_app/" + fire!!.name + "_" + mission.name + ".zip");

                    startActivity(sendIntent)
                    Log.i("action_import1","listo");

                    */
                } else util.showToast("Misión no importada")
                return true
            }
            R.id.action_usb -> {
                AsyncTask.execute {
                    var client = comm.getClientConnection()
                    if (client == null) {
                        util.showToast("Sin conexión a FireSense")
                    } else {
                        client.close()
                        // util.showToast("USB...")
                        if (comm.sendUsb(fire!!.name, mission.name).equals("success")) {
                            util.showToast("Misión enviada a USB con éxito...")
                        } else util.showToast("Error enviando a USB...")
                    }
                }
                return true
            }
            /*   R.id.action_edit -> {
                util.showToast("Editar")
                return true
            }*/
            R.id.action_deleted -> {
                // if (mission.state.equals("FINALIZED"))
                AsyncTask.execute {
                    var client = comm.getClientConnection()
                    // si no hay comunicacion con jetson
                    if (client == null) {
                        Log.i("conexion", "conexion")
                        util.showToast("Sin conexión a FireSense")
                        // firesJetson = comm.getFiresJetson(util, client)
                    }
                    // conexion con jetson
                    else {
                        client.close()
                        util.showToast("Eliminando misión...")
                        if (comm.delFire(fire!!.name, mission.name)) {
                            util.showToast("Misión eliminada...")
                            val intent = Intent(this@MissionActivity, MainActivity::class.java)
                            intent.putExtra(
                                    "firesJetson",
                                    util.delFireList(firesJetson, fire!!.name)
                            )
                            startActivity(intent)
                        }
                    }
                }
                // else{
                //      util.showToast("Misión no finalizada")
                // }
                // util.showToast("Eliminar")
                return true
            }
            R.id.action_deleted_local -> {
                util.showToast("Eliminando imagenes locales...")
                // util.isImported(mission.name,fire!!.name)
                util.deleteRecursiveMision(mission.name, fire!!.name, "RGB")
                util.deleteRecursiveMision(mission.name, fire!!.name, "THERMAL")
                util.deleteRecursiveMision(mission.name, fire!!.name, "MOSAICO")
                // util.deleteRecursiveMision(mission.name,fire!!.name)
                /* util.showToast("Mision eliminada...")
                Handler().postDelayed({
                    val intent = Intent(this@MissionActivity, MainActivity::class.java)
                    intent.putExtra("firesJetson", firesJetson)
                    startActivity(intent)
                    finish()
                }, 1000)*/
                return true
            }
            R.id.connectIcon -> {
                this.util.setIcon(this.menu_, this.comm, true)
                Handler()
                        .postDelayed(
                                {
                                    val intent =
                                            Intent(
                                                    this@MissionActivity,
                                                    ProgressActivity::class.java
                                            )
                                    intent.putExtra("activityTo", "mission")
                                    intent.putExtra("mission", mission)
                                    intent.putExtra("fire", fire)
                                    intent.putExtra("firesJetson", firesJetson)
                                    intent.putExtra("activityFrom", activityFrom)
                                    startActivity(intent)
                                },
                                1000
                        )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // util.showToast("BACK BOTTON")
        if (activityFrom.equals("MAIN")) {
            Handler()
                    .postDelayed(
                            {
                                val intent = Intent(this@MissionActivity, MainActivity::class.java)
                                intent.putExtra("firesJetson", firesJetson)
                                startActivity(intent)
                                finish()
                            },
                            500
                    )
        } else {
            Handler()
                    .postDelayed(
                            {
                                val intent = Intent(this@MissionActivity, FireActivity::class.java)
                                intent.putExtra("firesJetson", firesJetson)
                                intent.putExtra("fire", fire)
                                finish()
                            },
                            500
                    )
        }
    }

    private fun downloadFile(url: String, outputFile: File) {
        try {
            val u = URL(url)
            val conn: URLConnection = u.openConnection()
            Log.i("URLConnection", conn.toString())
            val contentLength: Int = conn.contentLength
            val stream = DataInputStream(u.openStream())
            val buffer = ByteArray(contentLength)
            stream.readFully(buffer)
            stream.close()
            val fos = DataOutputStream(FileOutputStream(outputFile))
            fos.write(buffer)
            fos.flush()
            fos.close()
            util.showToast("Importacion finalizada...")
        } catch (e: FileNotFoundException) {
            return // swallow a 404
        } catch (e: IOException) {
            return // swallow a 404
        }
    }
}
