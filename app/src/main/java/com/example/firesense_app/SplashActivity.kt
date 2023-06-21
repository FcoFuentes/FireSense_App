package com.example.firesense_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Utils
import kotlin.system.exitProcess
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {

    var resultList = ArrayList<ScanResult>()
    lateinit var wifiManager: WifiManager

    // Banderas que indicarán si tenemos permisos
    private val tienePermisoCamara = false

    // Banderas que indicarán si tenemos permisos
    private var tienePermisoAlmacenamiento = false

    // Código de permiso, defínelo tú mismo
    private val CODIGO_PERMISOS_CAMARA = 1

    // Código de permiso, defínelo tú mismo
    private val CODIGO_PERMISOS_ALMACENAMIENTO = 2

    var firesJetson = ArrayList<Fire>()
    var comm = Communication()
    val util = Utils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        verificarYPedirPermisosDeAlmacenamiento()
        //  comm.settingJson()

        /*   AsyncTask.execute {
            comm.getInfo()
        }*/
        setContentView(R.layout.activity_splash)
        supportActionBar!!.title = ""

        // crea directorio en media memoria interna
        /* util.createRaizDir()
        this.wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        //verifica si h
        util.isConnectingToInternet()
        AsyncTask.execute {
            var client = comm.getClientConnection()
            //si no hay comunicacion con jetson
            if(client == null) {
                Log.i("conexion", "conexion")
                showToast("Sin conexion a jetson")
                firesJetson = comm.getFiresJetson(util, client)
            }
            //conexion con jetson
            else{
                showToast("con conexion a jetson")
                firesJetson = comm.getFiresJetson(util, client)
            }
        }
        Handler().postDelayed({
            //start main activity
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            //intent.putExtra("fires","gola")
            // intent.putExtra("fires",fires)
            intent.putExtra("firesJetson", firesJetson)
            // intent.putExtra("util",fires)
            startActivity(intent)
            //finish this activity
            finish()
        }, 4000)*/
    }

    private fun verificarYPedirPermisosDeAlmacenamiento() {
        val estadoDePermiso: Int =
                ContextCompat.checkSelfPermission(
                        this@SplashActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        Log.i("verificarYPedirPermisosDeAlmacenamiento", estadoDePermiso.toString())
        Log.i(
                "verificarYPedirPermisosDeAlmacenamiento",
                PackageManager.PERMISSION_GRANTED.toString()
        )
        if (estadoDePermiso == PackageManager.PERMISSION_GRANTED) {
            Log.i("verificarYPedirPermisosDeAlmacenamiento", "if 1")
            // En caso de que haya dado permisos ponemos la bandera en true
            // y llamar al método
            permisoDeAlmacenamientoConcedido()
        } else {
            Log.i("verificarYPedirPermisosDeAlmacenamiento", "if 2")
            // Si no, entonces pedimos permisos. Ahora mira onRequestPermissionsResult
            ActivityCompat.requestPermissions(
                    this@SplashActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CODIGO_PERMISOS_ALMACENAMIENTO
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            CODIGO_PERMISOS_ALMACENAMIENTO ->
                    if (grantResults.size > 0 &&
                                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ) {
                        permisoDeAlmacenamientoConcedido()
                    } else {
                        permisoDeAlmacenamientoDenegado()
                    }
        }
    }

    // si los permisos son consedidos
    private fun permisoDeAlmacenamientoConcedido() {
        Log.i("permisoDeAlmacenamientoConcedido", "conexion")
        util.createRaizDir()
        util.isConnectingToInternet()
        var jsonObject = JSONObject()
        AsyncTask.execute {
            var client = comm.getClientConnection()

            // si no hay comunicacion con jetson
            if (client == null) {
                //   client!!.close()
                Log.i("conexion", "conexionasd")
                showToast("Sin conexión a FireSense")
                firesJetson = util.readFires()
                // firesJetson = comm.getFiresJetson(util, client)
            }
            // conexion con jetson
            else {
                // showToast("con conexion a jetson")
                firesJetson = comm.getFiresJetson(util, client)
                client.close()
            }
        }
        Handler()
                .postDelayed(
                        {
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            intent.putExtra("firesJetson", firesJetson)
                            intent.putExtra("info", jsonObject.toString())
                            startActivity(intent)
                            finish()
                        },
                        4000
                )
        tienePermisoAlmacenamiento = true
    }

    private fun permisoDeAlmacenamientoDenegado() {
        Log.i("permisoDeAlmacenamientoDenegado", "permisoDeAlmacenamientoDenegado")
        exitProcess(-1)
        // Esto se llama cuando el usuario hace click en "Denegar" o
        // cuando lo denegó anteriormente
        /*  Toast.makeText(
            this@SplashActivity,
            "El permiso para el almacenamiento está denegado",
            Toast.LENGTH_SHORT
        ).show()*/
    }

    fun showToast(msj: String) {
        this@SplashActivity.runOnUiThread(
                Runnable { Toast.makeText(this@SplashActivity, msj, Toast.LENGTH_SHORT).show() }
        )
    }
}
