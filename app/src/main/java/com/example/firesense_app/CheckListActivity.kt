package com.example.firesense_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_checklist.*

class CheckListActivity : AppCompatActivity() {
    lateinit var menu_: Menu
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)
        getSupportActionBar()!!.setTitle("Checklist para vuelo")
        var checkBox_batery = findViewById(R.id.checkBox_batery) as CheckBox
        val checkBox_helice = findViewById(R.id.checkBox_helice) as CheckBox
        val checkBox_remoteControl = findViewById(R.id.checkBox_remoteControl) as CheckBox
        val checkBox_ipad = findViewById(R.id.checkBox_ipad) as CheckBox
        val checkBox_cableJetson = findViewById(R.id.checkBox_cableJetson) as CheckBox
        //  val checkBox5 = findViewById(R.id.checkBox5) as CheckBox
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_checklist, menu)
        menu_ = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // util.showToast("onOptionsItemSelected")
        // Handle action bar item clicks here.
        val id = item.getItemId()
        if (id == R.id.reload) {
            this.checkBox_batery.setChecked(false)
            this.checkBox_helice.setChecked(false)
            this.checkBox_remoteControl.setChecked(false)
            this.checkBox_ipad.setChecked(false)
            this.checkBox_cableJetson.setChecked(false)
            // this.checkBox5.setChecked(false);
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
