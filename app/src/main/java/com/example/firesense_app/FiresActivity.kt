package com.example.firesense_app

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.materialspinner.MaterialSpinner
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Utils
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FiresActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var menu_: Menu
    var firesRecycler: RecyclerView? = null
    var recyclerviewAdapterFires: RecyclerviewAdapterFires? = null
    var searchView: EditText? = null
    var search: CharSequence = ""
    var comm = Communication()

    // textview pare fecha desde
    var oldDate: TextView? = null

    // textview para fecha hasta
    var currentDate: TextView? = null
    var fireNumber: TextView? = null

    // array con las localidades
    lateinit var list_of_items: Array<String>
    private lateinit var spinner: MaterialSpinner

    // array para los incendios
    private lateinit var firesJetson: ArrayList<Fire>
    val util = Utils(this)

    // incendios filtrados
    var firesFiltered = ArrayList<Fire>()

    // calendario para desde
    var calDesde = Calendar.getInstance()

    // calendario para hasta
    var calHasta = Calendar.getInstance()
    private lateinit var locationStr: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fires)
        var res: Resources = resources
        // se llena el arary de localidades
        list_of_items = res.getStringArray(R.array.comunas)
        // cambiamos el "seleccione localidad por todas..."
        list_of_items.set(0, "Todas...")
        locationStr = list_of_items[0]
        spinner = findViewById(R.id.material_spinner)
        spinner.getSpinner().onItemSelectedListener = this

        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(aa)
        // cambiamos el titulo del activity
        supportActionBar!!.title = "Incendios"
        searchView = findViewById<EditText>(R.id.search_bar)
        firesJetson = intent.getSerializableExtra("firesJetson") as ArrayList<Fire>
        oldDate = findViewById<TextView>(R.id.date_desde_txt)
        oldDate!!.text = util.oldDate(firesJetson)
        currentDate = findViewById<TextView>(R.id.date_hasta_txt)
        currentDate!!.text = util.currentDate(firesJetson)
        fireNumber = findViewById<TextView>(R.id.mission_number)
        fireNumber!!.text = firesJetson.size.toString()
        // datepicker para seleccionar el desde
        val dateSetListenerDesde =
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                            view: DatePicker,
                            year: Int,
                            monthOfYear: Int,
                            dayOfMonth: Int
                    ) {
                        calDesde.set(Calendar.YEAR, year)
                        calDesde.set(Calendar.MONTH, monthOfYear)
                        calDesde.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        updateDateDesde()
                        filteringFires()
                    }
                }

        // datepicker para seleccionar el hasta
        val dateSetListenerHasta =
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                            view: DatePicker,
                            year: Int,
                            monthOfYear: Int,
                            dayOfMonth: Int
                    ) {
                        calHasta.set(Calendar.YEAR, year)
                        calHasta.set(Calendar.MONTH, monthOfYear)
                        calHasta.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        updateDateHasta()
                        filteringFires()
                    }
                }

        val hasta = findViewById<LinearLayout>(R.id.date_hasta)
        hasta.setOnClickListener {
            // util.showToast("SELECCIONAR FECHA HASTA")
            var split = oldDate!!.text.split("-")
            DatePickerDialog(
                            this@FiresActivity,
                            dateSetListenerHasta,
                            calHasta.get(Calendar.YEAR),
                            calHasta.get(Calendar.MONTH),
                            calHasta.get(Calendar.DAY_OF_MONTH)
                    )
                    .show()
        }

        val desde = findViewById<LinearLayout>(R.id.date_desde)
        desde.setOnClickListener {
            // util.showToast("SELECCIONAR FECHA DESDE")
            var split = oldDate!!.text.split("-")
            DatePickerDialog(
                            this@FiresActivity,
                            dateSetListenerDesde,
                            calHasta.get(Calendar.YEAR),
                            calHasta.get(Calendar.MONTH),
                            calHasta.get(Calendar.DAY_OF_MONTH)
                    )
                    .show()
        }
        //      firesFiltered.add(firesJetson.get(0))
        //        firesFiltered.add(firesJetson.get(2))

        setFireRecycler(firesJetson)
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
                        recyclerviewAdapterFires?.filter?.filter(charSequence)
                        // recyclerviewAdapter.getFilter().filter(charSequence)
                        search = charSequence
                    }

                    override fun afterTextChanged(editable: Editable) {}
                }
        )
    }

    fun filteringFires() {
        Log.i("filteringFires", "filteringFires")
        var f = oldDate!!.text.split("/")
        var f1 = currentDate!!.text.split("/")
        Log.i("filteringFiresF", f.toString())
        Log.i("filteringFiresF1", f1.toString())
        firesFiltered.clear()
        for (fire in firesJetson) {
            var fecha: LocalDateTime =
                    LocalDateTime.of(f[2].toInt(), f[1].toInt(), f[0].toInt(), 0, 0, 0)
            var fecha1: LocalDateTime =
                    LocalDateTime.of(f1[2].toInt(), f1[1].toInt(), f1[0].toInt(), 0, 0, 0)
            Log.i(
                    "fire.date.isAfter(fecha)",
                    fire.date.isAfter(fecha).toString() +
                            " / " +
                            fire.date.toString() +
                            " > " +
                            fecha.toString()
            )
            Log.i(
                    "fire.date.isBefore(fecha1)",
                    fire.date.isBefore(fecha1).toString() +
                            " / " +
                            fire.date.toString() +
                            " < " +
                            fecha1.toString()
            )
            if (fire.date.isAfter(fecha) && fire.date.isBefore(fecha1.plusDays(1))) {
                if (locationStr.equals(fire.locationPlace)) firesFiltered.add(fire)
                else if (locationStr.equals("Todas...")) firesFiltered.add(fire)
                Log.i("filteringFires!!", fire.name)
            }
        }
        setFireRecycler(firesFiltered)
        fireNumber!!.text = firesFiltered.size.toString()
    }

    // actualiza textview desde
    private fun updateDateDesde() {
        val myFormat = "dd/MM/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        oldDate!!.text = sdf.format(calDesde.time)
    }

    // actualiza textview hasta
    private fun updateDateHasta() {
        val myFormat = "dd/MM/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        currentDate!!.text = sdf.format(calHasta.time)
    }

    private fun setFireRecycler(fireDataList: ArrayList<Fire>) {
        firesRecycler = findViewById<RecyclerView>(R.id.fireRecycler)
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        firesRecycler!!.layoutManager = layoutManager
        recyclerviewAdapterFires = RecyclerviewAdapterFires(this, fireDataList)
        firesRecycler!!.adapter = recyclerviewAdapterFires
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_fires, menu)
        menu_ = menu
        this.util.setIcon(this.menu_, this.comm, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.connectIcon) {
            this.util.setIcon(this.menu_, this.comm, true)
            return true
        }
        if (id == R.id.addFire) {
            AsyncTask.execute {
                var client = comm.getClientConnection()
                // si no hay comunicacion con jetson
                if (client == null) {
                    util.showToast("Sin conexión a FireSense")
                } else {
                    client.close()
                    val intent = Intent(this@FiresActivity, NewFireActivity::class.java)
                    intent.putExtra("firesJetson", firesJetson)
                    startActivity(intent)
                }
            }
            //   util.showToast("add fire")
            return true
        }
        /* if (id == R.id.action_three) {
            Toast.makeText(this, "Item Three Clicked", Toast.LENGTH_LONG).show()
            return true
        }*/
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        spinner.setErrorEnabled(false)
        spinner.setLabel("Comuna")
        setFireRecycler(firesJetson)
        Log.i("Localidad", list_of_items.get(position).toString())
        /*  if (position == 0) {
            spinner.setError("Por favor seleccionar localidad")
            setFireRecycler(fires)
        }
        else {*/
        //  }
        locationStr = list_of_items.get(position)
        filteringFires()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
