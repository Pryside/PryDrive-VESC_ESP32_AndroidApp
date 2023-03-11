package com.example.bt_tester2

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bt_tester2.UIActivity
import java.util.*

open class MainActivity : AppCompatActivity() {
    private var listView: ListView? = null
    //private val aAdapter: ArrayAdapter<*>? = null
    //var btAdapter = BluetoothAdapter.getDefaultAdapter()
    //var btSocket: BluetoothSocket? = null
    var mac_addresses = ArrayList<String>()
    var device_names = ArrayList<String>()
    var sharedPref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById<View>(R.id.listView) as ListView
        sharedPref = super.getPreferences(MODE_PRIVATE)
        val savedmac = sharedPref?.getString(MAC_SAVE, "")
        var saveddevice = sharedPref?.getString(DEVICE_SAVE, "")
        saveddevice = "ESP32"
        if (savedmac != "") {
            StartUIActivity(saveddevice, savedmac)
        }
        println(savedmac)



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return
        }
        /*val pairedDevices = btAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            // There are paired devices. Get the name and address of each paired device.
            val list = ArrayList<String>()
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                list.add(
                    """
                        $deviceName
                        $deviceHardwareAddress
                        """.trimIndent()
                )
                mac_addresses.add(deviceHardwareAddress)
                device_names.add(deviceName)
            }
            val adapter = ArrayAdapter(this, R.layout.customlist, R.id.textviewer1, list)
            listView!!.adapter = adapter
        }*/
        listView!!.isClickable = true
        listView!!.onItemClickListener = OnItemClickListener { arg0, arg1, position, arg3 ->
            val devicename = device_names[position]
            val mac = mac_addresses[position]
            val editor = sharedPref?.edit()
            editor?.putString(MAC_SAVE, mac)
            editor?.putString(DEVICE_SAVE, devicename)
            editor?.apply()
            println(devicename + "devicename and " + mac + " mac saved")
            StartUIActivity(devicename, mac)
        }
    }

    fun StartUIActivity(devicename: String?, mac: String?) {
        val intent = Intent(listView!!.context, UIActivity::class.java)
        intent.putExtra("device", devicename)
        intent.putExtra("mac", mac)
        startActivity(intent)

    }

    companion object {
        val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val MAC_SAVE = "a1b2"
        const val DEVICE_SAVE = "a1b1"
    }
}