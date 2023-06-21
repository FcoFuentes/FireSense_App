package com.example.firesense_app

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Utils
import org.json.JSONObject

class InfoActivity : AppCompatActivity() {

    lateinit var menu_: Menu
    lateinit var total: CheckBox
    lateinit var disponible: CheckBox
    lateinit var usb: CheckBox
    lateinit var normal: CheckBox
    lateinit var termica: CheckBox
    lateinit var obj: JSONObject
    val util = Utils(this)
    var comm = Communication()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        supportActionBar!!.title = "Información de FireSense"
        total = findViewById<CheckBox>(R.id.total)
        disponible = findViewById<CheckBox>(R.id.disponible)
        usb = findViewById<CheckBox>(R.id.usb)
        normal = findViewById<CheckBox>(R.id.rgb)
        termica = findViewById<CheckBox>(R.id.termica)
        obj = JSONObject(intent.getStringExtra("info"))
        this.checked()
        this.setInfo()
    }

    fun checked() {
        this.total.isEnabled = false
        this.disponible.isEnabled = false
        this.usb.isEnabled = false
        this.normal.isEnabled = false
        this.termica.isEnabled = false
        this.total.isChecked = true
        this.disponible.isChecked = true
        this.usb.isChecked = true
        this.normal.isChecked = true
        this.termica.isChecked = true
    }

    fun setInfo() {
        Log.i("JSON: ", this.obj.toString())
        Log.i("capacity: ", this.obj.get("capacity").toString())
        this.total.text = "Espacio total: " + this.obj.get("capacity").toString() + "MB"
        this.disponible.text = "Espacio disponible: " + this.obj.get("available").toString() + "MB"

        if (this.obj.get("usb").equals("FAIL") || this.obj.get("usb").equals("..")) {
            this.usb.text = "USB: No conectado"
            this.usb.isChecked = false
        } else this.usb.text = "USB: Conectado - " + this.obj.get("usb")

        if (this.obj.get("rgb").equals("FAIL")) {
            this.normal.text = "Cámara normal: No detectada"
            this.normal.isChecked = false
        }
        if (this.obj.get("thermal").equals("FAIL")) {
            this.termica.text = "Cámara termica: No detectada"
            this.termica.isChecked = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        menu_ = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // util.showToast("onOptionsItemSelected")
        // Handle action bar item clicks here.
        when (item.itemId) {
            R.id.reiniciar -> {
                util.showToast("reiniciar")
                AsyncTask.execute {
                    var client = comm.getClientConnection()

                    if (client == null) {
                        util.showToast("sin conexion")
                    } else {
                        if (comm.executeAction(client, Command.REBOOT))
                                util.showToast("Reiniciando Firesense...")
                        client.close()
                    }
                }
                goMain()
                return true
            }
            R.id.apagar -> {
                AsyncTask.execute {
                    var client = comm.getClientConnection()
                    // si no hay comunicacion con jetson
                    if (client == null) {
                        util.showToast("sin conexion")
                    } else {
                        if (comm.executeAction(client, Command.SHUTDOWN)) {}
                        util.showToast("Reiniciando Firesense...")
                        client.close()
                    }
                }
                goMain()
                return true
            }
            R.id.cleanIcon -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    fun goMain() {
        Handler()
                .postDelayed(
                        {
                            val intent = Intent(this@InfoActivity, MainActivity::class.java)
                            intent.putExtra("firesJetson", util.readFires())
                            startActivity(intent)
                            finish()
                        },
                        2000
                )
    }
}
