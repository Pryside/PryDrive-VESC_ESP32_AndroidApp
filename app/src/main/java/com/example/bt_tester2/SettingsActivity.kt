package com.example.bt_tester2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.bt_tester2.UIActivity

class SettingsActivity : AppCompatActivity() {
    val endofsend = "set\n"
    var settingspref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val sw1 = findViewById<Switch>(R.id.set1switch)
        val sw2 = findViewById<Switch>(R.id.set2switch)
        val rd1 = findViewById<RadioButton>(R.id.radioButton1)
        val rd2 = findViewById<RadioButton>(R.id.radioButton2)
        val rd3 = findViewById<RadioButton>(R.id.radioButton3)
        val rd4 = findViewById<RadioButton>(R.id.radioButton4)
        val rd = findViewById<RadioGroup>(R.id.radiogroup)
        settingspref = super.getPreferences(MODE_PRIVATE)
        val check_settings = settingspref?.getString(SETTINGS_CHK, "")
        if (check_settings == set_checkword) {
            //checked if settings were saved, can be loaded
            sw1.isChecked = settingspref?.getBoolean(SETTINGS_sw1, false) == true
            rd1.isChecked = settingspref?.getBoolean(SETTINGS_rd1, false) == true
            rd2.isChecked = settingspref?.getBoolean(SETTINGS_rd2, false) == true
            rd3.isChecked = settingspref?.getBoolean(SETTINGS_rd3, false) == true
            rd4.isChecked = settingspref?.getBoolean(SETTINGS_rd4, false) == true
            sw2.isChecked = settingspref?.getBoolean(SETTINGS_sw2, false) == true
        }
        val savebtn = findViewById<View>(R.id.savebutton) as Button
        savebtn.setOnClickListener {
            var settings_data = ""
            settings_data += Integer.valueOf(boolToInt(sw1.isChecked)).toString()
            settings_data += Integer.valueOf(rd.indexOfChild(findViewById<View>(rd.checkedRadioButtonId)) + 1)
                .toString()
            settings_data += Integer.valueOf(boolToInt(sw2.isChecked)).toString()
            settings_data += endofsend
            val intent = Intent(savebtn.context, UIActivity::class.java)
            intent.putExtra("settings", settings_data)
            setResult(RESULT_OK, intent)
            finish()
            println("saved")
            val settingseditor = settingspref?.edit()
            settingseditor?.putBoolean(SETTINGS_sw1, sw1.isChecked)
            settingseditor?.putBoolean(SETTINGS_rd1, rd1.isChecked)
            settingseditor?.putBoolean(SETTINGS_rd2, rd2.isChecked)
            settingseditor?.putBoolean(SETTINGS_rd3, rd3.isChecked)
            settingseditor?.putBoolean(SETTINGS_rd4, rd4.isChecked)
            settingseditor?.putBoolean(SETTINGS_sw2, sw2.isChecked)
            settingseditor?.putString(SETTINGS_CHK, set_checkword)
            settingseditor?.apply()
        }
    }

    fun boolToInt(b: Boolean): Int {
        return java.lang.Boolean.compare(b, false)
    }

    companion object {
        const val SETTINGS_CHK = "a1set00"
        const val set_checkword = "this is set"
        const val SETTINGS_sw1 = "a1sw1"
        const val SETTINGS_sw2 = "a1sw2"
        const val SETTINGS_rd1 = "a1srd1"
        const val SETTINGS_rd2 = "a1srd2"
        const val SETTINGS_rd3 = "a1srd3"
        const val SETTINGS_rd4 = "a1srd4"
    }
}