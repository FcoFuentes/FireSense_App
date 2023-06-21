package com.example.firesense_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NewMissionActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var comm = Communication()
    val util = Utils(this)
    // private var list_of_items = arrayOf("Seleccionar incendio") //lista de string para spinner

    private lateinit var spinner: MaterialSpinner
    private lateinit var spinnerAltura: MaterialSpinner

    private var fireStr = "Seleccionar incendio"

    private lateinit var firesJetson: ArrayList<Fire>
    private var fire: Fire? = null

    private var names = ArrayList<String>()

    lateinit var latitud: TextView
    lateinit var longitud: TextView
    lateinit var direccion: TextView
    lateinit var description: TextView
    lateinit var menu_: Menu

    private val ALTURAS = arrayOf("100", "150", "200", "250", "300")
    private var altura = ALTURAS.get(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_mission)
        supportActionBar!!.title = "Crear misión"

        // lista de incendios
        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        fire = intent.getSerializableExtra("fire") as Fire?

        latitud = findViewById<EditText>(R.id.txtLat)
        longitud = findViewById<EditText>(R.id.txtLon)
        direccion = findViewById<EditText>(R.id.txtDireccion)
        description = findViewById<EditText>(R.id.txtDescription)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val labelDate: EditText = findViewById(R.id.txtDate)
        labelDate.setText(formatted)
        if (fire != null) {
            names.add(fire!!.name)
        } else {
            names.add("Seleccionar incendio")
            for (fire in firesJetson) {
                names.add(fire.name)
            }
        }

        spinner = findViewById(R.id.material_spinner)
        spinner.getSpinner().onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(aa)
        spinner.setLabel("Incendio")

        val butoton: Button = findViewById(R.id.btnSend)
        butoton.setOnClickListener {
            val name: EditText = findViewById(R.id.txtName)
            val date: EditText = findViewById(R.id.txtDate)
            val description: EditText = findViewById(R.id.txtDescription)

            if (!(name.text.isBlank()) &&
                            !(labelDate.text.isBlank()) &&
                            !(description.text.isBlank()) &&
                            !fireStr.equals("Seleccionar incendio")
            ) {
                fire = getFire(fireStr)
                if (util.existMission(fire!!.missions, name.text.toString()))
                        util.showToast("La misión ya existe")
                else
                        AsyncTask.execute {
                            var client = comm.getClientConnection()
                            // si no hay comunicacion con jetson
                            if (client == null) {

                                util.showToast("Sin conexión a Jetson")
                            } else {
                                val mission =
                                        Mission(
                                                name.text.toString(),
                                                LocalDateTime.parse(LocalDateTime.now().toString()),
                                                altura.toInt(),
                                                latitud.text.toString().toDouble(),
                                                longitud.text.toString().toDouble(),
                                                fireStr,
                                                description.text.toString(),
                                                R.drawable.drone1,
                                                "CREATED"
                                        )
                                // util.showToast("CREANDO MISION")
                                if (comm.sendMission(mission, fireStr, client)) {
                                    // util.showToast("sendMission")
                                    if (fire == null) fire = getFire(fireStr)
                                    else fire = getFire(fire!!.name)
                                    fire!!.addMission(mission)
                                    util.createMission(
                                            mission.name,
                                            mission.fireName,
                                            mission.getJson(mission.fireName)
                                    )
                                    val intent = Intent(this, MainActivity::class.java)
                                    Timer().schedule(
                                                    object : TimerTask() {
                                                        override fun run() {
                                                            // util.showToast("Incendio creado")
                                                            intent.putExtra(
                                                                    "firesJetson",
                                                                    firesJetson
                                                            )
                                                            intent.putExtra("fire", fire)
                                                            startActivity(intent)
                                                        }
                                                    },
                                                    1000
                                            )
                                }
                            }
                        }
                this.util.setIcon(this.menu_, this.comm, false)
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

    fun getFire(fireName: String): Fire? {
        for (fire in firesJetson) if (fire.name.equals(fireName)) return fire
        return null
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if (fire == null && position == 0) spinner.setError("Por favor seleccionar incendio")
        else {
            spinner.setErrorEnabled(false)
        }
        fireStr = names.get(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_mission, menu)
        menu_ = menu
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
        lateinit var mainActivity: NewMissionActivity

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
