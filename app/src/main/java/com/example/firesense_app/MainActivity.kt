package com.example.firesense_app

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils

class MainActivity : AppCompatActivity() {
    val util = Utils(this)
    private var mOptionsMenu: Menu? = null
    var comm = Communication()
    var txtLastMission: TextView? = null

    var mission: Mission? = null
    var fire: Fire? = null

    lateinit var menu_: Menu
    lateinit var firesJetson: ArrayList<Fire>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title = "Principal"
        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>

        mission = util.getLastMission(firesJetson)
        fire = util.getFire(firesJetson, mission)

        txtLastMission = findViewById(R.id.last_mission_txt) // as TextView

        if (mission == null) txtLastMission!!.text = "No hay misiones"
        else {
            var state: String = ""
            if (mission!!.state.equals("CREATED")) state = "Volando" else state = "Finalizado"
            txtLastMission!!.text = mission!!.name + " -  " + fire!!.name + " - " + state
        }
        val cardViewLastMission = findViewById<CardView>(R.id.last_mission)
        cardViewLastMission.setOnClickListener {
            ///            var mission = util.getLastMission(firesJetson)
            if (mission != null) {
                val intent = Intent(this@MainActivity, ProgressActivity::class.java)
                intent.putExtra("activityTo", "mission")
                intent.putExtra("mission", mission)
                intent.putExtra("fire", fire)
                intent.putExtra("firesJetson", firesJetson)
                intent.putExtra("activityFrom", "MAIN")
                startActivity(intent)
            } else util.showToast("No existen misiones")
        }

        val cardViewNewFire = findViewById<CardView>(R.id.new_fire)
        cardViewNewFire.setOnClickListener {
            AsyncTask.execute {
                var client = comm.getClientConnection()
                if (client == null) {
                    util.showToast("Sin conexión a FireSense")
                    // client!!.close()
                } else if (mission != null) {
                    client.close()
                    //  if (mission!!.state.equals("FINALIZED")) {
                    val intent = Intent(this@MainActivity, NewFireActivity::class.java)
                    intent.putExtra("firesJetson", firesJetson)
                    startActivity(intent)
                } else {
                    client.close()
                    val intent = Intent(this@MainActivity, NewFireActivity::class.java)
                    intent.putExtra("firesJetson", firesJetson)
                    startActivity(intent)
                }
            }
            this.util.setIcon(this.menu_, this.comm, false)
        }

        val cardViewNewMissionActivity = findViewById<CardView>(R.id.new_mission)
        cardViewNewMissionActivity.setOnClickListener {
            AsyncTask.execute {
                var client = comm.getClientConnection()
                // si no hay comunicacion con jetson
                if (client == null) {
                    util.showToast("Sin conexión a FireSense")
                    // client!!.close()
                } else if (!firesJetson.isEmpty()) {
                    client.close()
                    if (mission != null) {
                        //      if (mission!!.state.equals("FINALIZED")) {
                        val intent = Intent(this@MainActivity, NewMissionActivity::class.java)
                        intent.putExtra("firesJetson", firesJetson)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MainActivity, NewMissionActivity::class.java)
                        intent.putExtra("firesJetson", firesJetson)
                        startActivity(intent)
                    }
                } else {
                    util.showToast("Primero cree un incendio")
                    client.close()
                }
            }
            this.util.setIcon(this.menu_, this.comm, false)
        }

        val cardViewNewFiresActivity = findViewById<CardView>(R.id.fires)
        cardViewNewFiresActivity.setOnClickListener {
            if (firesJetson.isEmpty()) util.showToast("No existen incendio")
            else {
                val intent = Intent(this@MainActivity, FiresActivity::class.java)
                intent.putExtra("firesJetson", firesJetson)
                startActivity(intent)
            }
        }

        val cardViewSettings = findViewById<CardView>(R.id.settings)
        cardViewSettings.setOnClickListener {
            AsyncTask.execute {
                Log.i("3", "SETTING AsyncTask")
                var client = comm.getClientConnection()
                // si no hay comunicacion con jetson
                if (client == null) {
                    util.showToast("Sin conexión a FireSense")
                } else {
                    client.close()
                    val intent = Intent(this@MainActivity, ProgressActivity::class.java)
                    intent.putExtra("activityTo", "settings")
                    intent.putExtra("firesJetson", firesJetson)
                    intent.putExtra("activityFrom", "MAIN")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu_ = menu
        this.util.setIcon(this.menu_, this.comm, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // util.showToast("onOptionsItemSelected")
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.connectIcon) {
            this.util.setIcon(this.menu_, this.comm, true)
            this.updateFires()
            return true
        } else if (id == R.id.checklist) {
            val intent = Intent(this@MainActivity, CheckListActivity::class.java)
            // intent.putExtra("firesJetson", firesJetson)
            startActivity(intent)
            return true
        } else if (id == R.id.manual) {
            // Log.i("3", "checklist")
            val intent = Intent(this@MainActivity, ManualActivity::class.java)
            // intent.putExtra("firesJetson", firesJetson)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateFires() {
        AsyncTask.execute {
            var client = comm.getClientConnection()
            // si no hay comunicacion con jetson
            if (client == null) {
                //   client!!.close()
                // showToast("Sin conexion a jetson")
                this.firesJetson = util.readFires()
                // firesJetson = comm.getFiresJetson(util, client)
            }
            // conexion con jetson
            else {
                // showToast("con conexion a jetson")
                this.firesJetson = comm.getFiresJetson(util, client)
                client.close()
            }
            mission = util.getLastMission(firesJetson)
            fire = util.getFire(firesJetson, mission)
        }
        Handler()
                .postDelayed(
                        {
                            if (mission == null) txtLastMission!!.text = "No hay misiones"
                            else {
                                var state: String = ""
                                if (mission!!.state.equals("CREATED")) state = "Volando"
                                else state = "Finalizado"
                                txtLastMission!!.text =
                                        mission!!.name + " -  " + fire!!.name + " - " + state
                            }
                        },
                        2000
                )
    }
}
