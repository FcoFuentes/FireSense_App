package com.example.firesense_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Utils
import org.json.JSONObject

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    val util = Utils(this)
    var comm = Communication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar!!.title = "Configuración"
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
        val obj = JSONObject(intent.getStringExtra("info"))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.i("onSharedPreferenceChanged", key)
        val pref = sharedPreferences?.getString(key, "1")
        Log.i("onSharedPreferenceChanged", pref)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
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
        }
        return super.onOptionsItemSelected(item)
    }

    fun goMain() {
        Handler()
                .postDelayed(
                        {
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            intent.putExtra("firesJetson", util.readFires())
                            startActivity(intent)
                            finish()
                        },
                        2000
                )
    }
}
