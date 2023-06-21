package com.example.firesense_app

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils
import java.time.format.DateTimeFormatter
import java.util.ArrayList

class FireActivity : AppCompatActivity() {

    var userRecycler: RecyclerView? = null
    var recyclerviewAdapterMission: RecyclerviewAdapterMission? = null
    var searchView: EditText? = null
    var search: CharSequence = ""
    var txtFireName: TextView? = null
    var txtLocateName: TextView? = null
    var txtMissionNumber: TextView? = null
    var txtDateFire: TextView? = null
    var txtTimeFire: TextView? = null
    val util = Utils(this)
    var comm = Communication()
    lateinit var menu_: Menu

    private lateinit var firesJetson: ArrayList<Fire>
    private lateinit var fire: Fire

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire)
        getSupportActionBar()!!.setTitle("Incendio")

        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        fire = intent.getSerializableExtra("fire") as Fire
        searchView = findViewById(R.id.search_bar) as EditText
        txtFireName = findViewById(R.id.fire_name) as TextView
        txtLocateName = findViewById(R.id.locate_name) as TextView
        txtMissionNumber = findViewById(R.id.mission_number) as TextView
        txtDateFire = findViewById(R.id.date_fire) as TextView
        txtTimeFire = findViewById(R.id.time_fire) as TextView
        txtFireName!!.text = fire.name
        txtLocateName!!.text = fire.locationPlace
        txtMissionNumber!!.text = fire.missions.size.toString()

        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd") // HH:mm:ss
        val formatterHour = DateTimeFormatter.ofPattern("HH:mm")

        txtDateFire!!.text = fire.date.format(formatterDate) // fire.date.toString()
        txtTimeFire!!.text = fire.date.format(formatterHour)

        setFireRecycler(fire.missions)

        searchView!!.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int
                    ) {}
                    override fun onTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int
                    ) {
                        recyclerviewAdapterMission?.filter?.filter(charSequence)
                        search = charSequence
                    }
                    override fun afterTextChanged(editable: Editable) {}
                }
        )
    }

    private fun setFireRecycler(MissionDataList: ArrayList<Mission>) {
        userRecycler = findViewById(R.id.fireRecycler) as RecyclerView
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        userRecycler!!.setLayoutManager(layoutManager)
        recyclerviewAdapterMission = RecyclerviewAdapterMission(this, fire, firesJetson)
        userRecycler!!.setAdapter(recyclerviewAdapterMission)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_fire, menu)
        menu_ = menu
        this.util.setIcon(this.menu_, this.comm, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()
        if (id == R.id.connectIcon) {
            this.util.setIcon(this.menu_, this.comm, true)
            // this.updateFires()
            return true
        }
        if (id == R.id.addMission) {
            AsyncTask.execute {
                var client = comm.getClientConnection()
                // si no hay comunicacion con jetson
                if (client == null) {
                    util.showToast("Sin conexión a FireSense")
                } else {
                    client!!.close()
                    val intent = Intent(this, NewMissionActivity::class.java)
                    intent.putExtra("firesJetson", firesJetson)
                    intent.putExtra("fire", fire)
                    startActivity(intent)
                }
            }
            // util.showToast("No existen incendio")
            return true
        }
        /*if (id == R.id.action_edit) {
            util.showToast("Editar")
            return true
        }*/
        if (id == R.id.action_deleted) {
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
                    if (comm.delFire(fire.name, "")) {
                        util.showToast("Incendio " + fire.name + " eliminado.")
                        val intent = Intent(this@FireActivity, MainActivity::class.java)
                        intent.putExtra("firesJetson", util.delFireList(firesJetson, fire.name))
                        startActivity(intent)
                    }
                }
            }
            // util.showToast("Eliminar")
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
