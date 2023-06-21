package com.example.firesense_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.*
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dev.materialspinner.MaterialSpinner
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Utils
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NewFireActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var list_of_items: Array<String>
    private lateinit var spinner: MaterialSpinner
    private lateinit var locationStr: String

    var comm = Communication()
    val util = Utils(this)
    lateinit var latitud: TextView
    lateinit var longitud: TextView
    lateinit var direccion: TextView
    lateinit var description: TextView
    lateinit var menu_: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_fire)
        supportActionBar!!.title = "Crear incendio"
        var res: Resources = resources
        list_of_items = res.getStringArray(R.array.comunas)
        locationStr = list_of_items[0]
        latitud = findViewById<EditText>(R.id.txtLat)
        longitud = findViewById<EditText>(R.id.txtLon)
        direccion = findViewById<EditText>(R.id.txtDireccion)
        description = findViewById<EditText>(R.id.txtDescription)

        val firesJetson: ArrayList<Fire> =
                intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        val editText = findViewById<EditText>(R.id.txtName)
        // editText.hint = Html.fromHtml(getString(R.string.hints))
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val labelDate: EditText = findViewById(R.id.txtDate)
        labelDate.setText(formatted)
        spinner = findViewById(R.id.material_spinner)
        spinner.getSpinner().onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(aa)

        val butoton: Button = findViewById(R.id.btnSendFire)
        butoton.setOnClickListener {
            val name: EditText = findViewById(R.id.txtName)
            // val locate: EditText = findViewById(R.id.txtLocate)
            val description: EditText = findViewById(R.id.txtDescription)
            // val location: EditText = findViewById(R.id.txtLocate)

            if (!(name.text.isBlank()) &&
                            !(description.text.isBlank()) &&
                            !locationStr.equals(list_of_items[0])
            ) {
                if (util.existFire(firesJetson, name.text.toString()))
                        util.showToast("El incendio ya existe")
                else {
                    AsyncTask.execute {
                        var client = comm.getClientConnection()
                        // si no hay comunicacion con jetson
                        if (client == null) {

                            util.showToast("Sin conexión a Jetson")
                        }
                        // conexion con jetson
                        else {
                            val fire =
                                    Fire(
                                            name.text.toString(),
                                            LocalDateTime.parse(LocalDateTime.now().toString()),
                                            locationStr,
                                            latitud.text.toString().toDouble(),
                                            longitud.text.toString().toDouble(),
                                            description.text.toString(),
                                            R.drawable.fire2
                                    )
                            if (comm.sendFire(fire, client)) {
                                util.createFire(fire)
                                firesJetson.add(fire)
                                val intent = Intent(this, NewMissionActivity::class.java)
                                Timer().schedule(
                                                object : TimerTask() {
                                                    override fun run() {
                                                        util.showToast("Incendio creado")
                                                        intent.putExtra("firesJetson", firesJetson)
                                                        intent.putExtra("fire", fire)
                                                        startActivity(intent)
                                                    }
                                                },
                                                1000
                                        )
                            } else util.showToast("Error creando incendio")
                            client.close()
                        }
                    }
                    this.util.setIcon(this.menu_, this.comm, false)
                }
            } else util.showToast("Ingrese todos los datos")
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !==
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000
            )
        } else {
            locationStart()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) spinner.setError("Por favor seleccionar comuna")
        else {
            spinner.setErrorEnabled(false)
            spinner.setLabel("Comuna")
        }
        locationStr = list_of_items.get(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //  util.showToast("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_new_fire, menu)
        menu_ = menu
        // this.util.setIcon(this.menu_,this.comm)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.connectIcon) {
            this.util.setIcon(this.menu_, this.comm, true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun locationStart() {
        val mlocManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val local = Localizacion()
        local.mainActivity = this
        val gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !==
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000
            )
            return
        }
        mlocManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                local as LocationListener
        )
        mlocManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                local as LocationListener
        )
        latitud.text = "Localización agregada"
        direccion.text = ""
    }

    fun setLocation(loc: Location) {
        // Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.latitude != 0.0 && loc.longitude != 0.0) {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val list = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (!list.isEmpty()) {
                    val DirCalle = list[0]
                    direccion.text = DirCalle.getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart()
                return
            }
        }
    }

    class Localizacion : LocationListener {
        lateinit var mainActivity: NewFireActivity
        override fun onLocationChanged(loc: Location) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.latitude
            loc.longitude
            val sLatitud = loc.latitude.toString()
            val sLongitud = loc.longitude.toString()
            mainActivity.latitud.text = sLatitud
            mainActivity.longitud.text = sLongitud
            mainActivity.setLocation(loc)
        }
        override fun onProviderDisabled(provider: String) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mainActivity.latitud.text = "GPS Desactivado"
        }

        override fun onProviderEnabled(provider: String) {
            // Este metodo se ejecuta cuando el GPS es activado
            mainActivity.latitud.text = "GPS Activado"
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            when (status) {
                LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                LocationProvider.OUT_OF_SERVICE -> Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
                LocationProvider.TEMPORARILY_UNAVAILABLE ->
                        Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE")
            }
        }
    }
}
