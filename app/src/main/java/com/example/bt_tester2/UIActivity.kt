package com.example.bt_tester2

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val BLUETOOTH_ALL_PERMISSIONS_REQUEST_CODE = 3
private const val SERVICE_UUID = "25AE1441-05D3-4C5B-8281-93D4E07420CF"
private const val CHAR_FOR_WRITE_UUID = "25AE1443-05D3-4C5B-8281-93D4E07420CF"
private const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"





open class UIActivity : AppCompatActivity() {


    val Notify_UUIDs = arrayOf(
        "25AE1447-05D3-4C5B-8281-93D4E07420CF"  ,
        "25AE1448-05D3-4C5B-8281-93D4E07420CF"  ,
        "25AE1449-05D3-4C5B-8281-93D4E07420CF"  ,
        "25AE1446-05D3-4C5B-8281-93D4E07420CF"
    )

    val Read_UUIDs = arrayOf(
        "25AE1445-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1450-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1451-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1452-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1453-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1454-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1455-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1456-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1457-05D3-4C5B-8281-93D4E07420CF" ,
        "25AE1458-05D3-4C5B-8281-93D4E07420CF" ,
    )
    var bluetoothdata = FloatArray(14)

    val datapoints = 14

    inner class Data {
        var tempMotor = 0f
        var motorCurr = 0f
        var batCurr = 0f
        var kmh = 0f
        var inpVolt = 0f
        var ampHours = 0f
        var ampHoursC = 0f
        var km = 0f
        var wattHours = 0f
        var wattHoursC = 0f
        var totalWh = 0f
        var totalWhC = 0f
        var totalkm = 0f
        var power = 0f
        var maxpower = 0f
        var minpower = 0f
        var percent = 0f
    }

    var draw_counter = 0
    var btdata: Data = Data()
    var batbar: ProgressBar? = null
    var kmh: TextView? = null
    var Voltage: TextView? = null
    var Amps: TextView? = null
    var Distance: TextView? = null
    var TotalDistance: TextView? = null
    var MotorTemp: TextView? = null
    var Energy: TextView? = null
    var EnergyCharged: TextView? = null
    var TotalEnergy: TextView? = null
    var TotalEnergyCharged: TextView? = null
    var Power: TextView? = null
    var devicename: String? = null
    var mac: String? = null
    fun getPower(): Float {
        var returnvalue = 0.0F
        if (btdata.power > 0.0F) returnvalue = (btdata.power * (100.0f / btdata.maxpower))
        if (btdata.power < 0.0F) returnvalue = (btdata.power * (-100.0f / btdata.minpower))
        if (returnvalue < -100.0F) returnvalue = -100.0F
        if (returnvalue > 100.0F) returnvalue = 100.0F
        return returnvalue
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uiactivity)


        userWantsToScanAndConnect = true
        if (userWantsToScanAndConnect){
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(bleOnOffListener, filter)
        }
        else{
            unregisterReceiver(bleOnOffListener)
        }
        bleRestartLifecycle()
        appendLog("got here")





        val Settingsbutton = findViewById<ImageButton>(R.id.settingsbutton)
        Settingsbutton.setOnClickListener {
            val intent = Intent(Settingsbutton.context, SettingsActivity::class.java)

            //startActivity(intent);
            //mStartForResult.launch(Intent(Settingsbutton.context, SettingsActivity::class.java))
        }
        batbar = findViewById(R.id.batterybar)
        kmh = findViewById(R.id.speedtext)
        Voltage = findViewById(R.id.textVoltage)
        Amps = findViewById(R.id.textAmps)
        Distance = findViewById(R.id.textDistance)
        TotalDistance = findViewById(R.id.textTotalDistance)
        MotorTemp = findViewById(R.id.textMotorTemp)
        Energy = findViewById(R.id.textEnergy)
        EnergyCharged = findViewById(R.id.textEnergyCharged)
        TotalEnergy = findViewById(R.id.textTotalEnergy)
        TotalEnergyCharged = findViewById(R.id.textTotalEnergyCharged)
        Power = findViewById(R.id.textPower)
        val intent = intent
        devicename = intent.getStringExtra("device")
        mac = intent.getStringExtra("mac")
        println("trying to connect")
        ConnectBT()
        println("next")
        val mySwitch = findViewById<View>(R.id.enableswitch) as Switch
        if (mySwitch.isChecked) {
        }
        StartDrawing()

    }

    public override fun onDestroy() {
        bleEndLifecycle()
        super.onDestroy()
    }

    fun StartDrawing() {
        val myview = findViewById<GraphView>(R.id.relativeLayout)
        myview.invalidate()
        Thread {
            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        CalculateData()
                        DisplayData()
                    }
                    myview.setData(getPower())
                    myview.invalidate()
                }
            }, 10, 50)
            println("RUNNING")
        }.start()
    }

    fun ConnectBT() {
        var text: String? = null
        text = "Connecting to $devicename"
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG)
        toast.show()

        //myBLE.get_started(true)
    }

    fun CalculateData() {
        //Extract data into Floats
        //Write data into correct containers
        btdata.batCurr      = bluetoothdata[0]
        btdata.kmh          = bluetoothdata[1]
        btdata.inpVolt      = bluetoothdata[2]
        btdata.motorCurr    = bluetoothdata[3]

        btdata.tempMotor    = bluetoothdata[4]
        btdata.ampHours     = bluetoothdata[5]
        btdata.ampHoursC    = bluetoothdata[6]
        btdata.km           = bluetoothdata[7]
        btdata.wattHours    = bluetoothdata[8]
        btdata.wattHoursC   = bluetoothdata[9]
        btdata.totalWh      = bluetoothdata[10]
        btdata.totalWhC     = bluetoothdata[11]
        btdata.totalkm      = bluetoothdata[12]
        btdata.percent      = bluetoothdata[13]

        btdata.power = btdata.inpVolt * btdata.batCurr
        //power sizing for graph use
        if (btdata.power > btdata.maxpower) btdata.maxpower = btdata.power
        if (btdata.power < btdata.minpower) btdata.minpower = btdata.power
    }

    fun DisplayData() {
        batbar!!.progress = btdata.percent.toInt()
        Voltage!!.text = "🔋 " + btdata.inpVolt + "V " + btdata.percent.roundToInt() +"%"
        kmh!!.text = round(btdata.kmh.toDouble(), 1).toString() + ""
        Power!!.text = "⚡ " + btdata.power.roundToInt() + "W"
        Amps!!.text = "Battery " + round(btdata.batCurr.toDouble(), 1) + "A"
        Distance!!.text = "Trip " + round(btdata.km.toDouble(), 2) + "Km"
        TotalDistance!!.text = "Odo " + round(btdata.totalkm.toDouble(), 1) + "km"
        MotorTemp!!.text = "Motor " + round(btdata.tempMotor.toDouble(), 1) + "°C"
        Energy!!.text =
            "Energy " + round(btdata.wattHours.toDouble(), 2) + "Wh"
        EnergyCharged!!.text = "Reg. Energy " + round(btdata.wattHoursC.toDouble(), 2) + "Wh"
        TotalEnergy!!.text =
            "Energy tot " + round(btdata.totalWh.toDouble(), 1) + "Wh"
        TotalEnergyCharged!!.text =
            "EnergyReg tot " + round(btdata.totalWhC.toDouble(), 1) + "Wh"
    }

    fun ShowConnectStatus(connected: ByteArray) {
        var text2: String? = null
        var toast2 = Toast.makeText(applicationContext, text2, Toast.LENGTH_LONG)
        if (connected[0].toInt() == 0x01) {
            text2 = "Connection to $devicename suceeded!"

        } else text2 = "Connection to $devicename failed!"
        toast2 = Toast.makeText(applicationContext, text2, Toast.LENGTH_LONG)
        toast2.show()
    }


    companion object {
        private fun round(value: Double, precision: Int): Double {
            val scale = Math.pow(10.0, precision.toDouble()).toInt()
            return Math.round(value * scale).toDouble() / scale
        }
    }



    //################################################################################
    //                          BLE PART BELOW
    //################################################################################



    private var activityResultHandlers = mutableMapOf<Int, (Int) -> Unit>()
    private var permissionResultHandlers = mutableMapOf<Int, (Array<out String>, IntArray) -> Unit>()
    private var bleOnOffListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_ON -> {
                    appendLog("onReceive: Bluetooth ON")
                    if (lifecycleState == BLELifecycleState.Disconnected) {
                        bleRestartLifecycle()
                    }
                }
                BluetoothAdapter.STATE_OFF -> {
                    appendLog("onReceive: Bluetooth OFF")
                    bleEndLifecycle()
                }
            }
        }
    }


    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val BT_NOTIFYS = 4
    val BT_READS = 10
    val BT_VARS = 14

    private fun appendLog(message: String) {
        //Log.d("appendLog", message)
        println(message)
        /*
        runOnUiThread {
            val strTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            //textViewLog.text = textViewLog.text.toString() + "\n$strTime $message"

            // scroll after delay, because textView has to be updated first
            Handler().postDelayed({
                //scrollViewLog.fullScroll(View.FOCUS_DOWN)
            }, 16)
        }*/
    }

    enum class BLELifecycleState {
        Disconnected,
        Scanning,
        Connecting,
        ConnectedDiscovering,
        ConnectedSubscribing,
        Connected
    }

    private var lifecycleState = BLELifecycleState.Disconnected
        set(value) {
            field = value
            appendLog("status = $value")
            /*
            runOnUiThread {
                //textViewLifecycleState.text = "State: ${value.name}"
                if (value != BLELifecycleState.Connected) {
                    //textViewSubscription.text = getString(R.string.text_not_subscribed)
                }
            }*/
        }



    private var userWantsToScanAndConnect = false
    private var isScanning = false
    private var connectedGatt: BluetoothGatt? = null
    private var characteristicForWrite: BluetoothGattCharacteristic? = null

    private var characteristicsForNotify = arrayOf<BluetoothGattCharacteristic?>(null, null,
        null, null)
    //4 notifys
    private var characteristicsForRead = arrayOf<BluetoothGattCharacteristic?>(null, null, null,
        null, null, null, null, null, null, null)
    //10 reads



    fun readCharacteristics() {

        var gatt = connectedGatt ?: run {
            appendLog("ERROR: read failed, no connected device")
            return
        }
        for (i in Read_UUIDs.indices){

            Timer().schedule(100*i.toLong()) {
                //appendLog(Read_UUIDs[i].toString())
                var characteristic = characteristicsForRead[i]
                gatt.readCharacteristic(characteristicsForRead[i])
            }

            var characteristic = characteristicsForRead[i] ?: run {
                appendLog("ERROR: read failed, characteristic unavailable $Read_UUIDs[i]")
                return
            }
            if (!characteristic.isReadable()) {
                appendLog("ERROR: read failed, characteristic not readable $Read_UUIDs[i]")
                return
            }


        }
    }

    fun onTapWrite(view: View) {
        var gatt = connectedGatt ?: run {
            appendLog("ERROR: write failed, no connected device")
            return
        }
        var characteristic = characteristicForWrite ?:  run {
            appendLog("ERROR: write failed, characteristic unavailable $CHAR_FOR_WRITE_UUID")
            return
        }
        if (!characteristic.isWriteable()) {
            appendLog("ERROR: write failed, characteristic not writeable $CHAR_FOR_WRITE_UUID")
            return
        }
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        //characteristic.value = editTextWriteValue.text.toString().toByteArray(Charsets.UTF_8)
        //gatt.writeCharacteristic(characteristic)
    }

    private fun bleEndLifecycle() {
        safeStopBleScan()
        connectedGatt?.close()
        setConnectedGattToNull()
        lifecycleState = BLELifecycleState.Disconnected
    }

    private fun setConnectedGattToNull() {
        connectedGatt = null
        characteristicForWrite = null
        //characteristicForIndicate = null

        for (i in characteristicsForNotify.indices){
            characteristicsForNotify[i] = null
        }
        for (i in characteristicsForRead.indices){
            characteristicsForRead[i] = null
        }

    }

    private fun bleRestartLifecycle() {
        runOnUiThread {
            if (userWantsToScanAndConnect) {
                if (connectedGatt == null) {
                    prepareAndStartBleScan()
                } else {
                    connectedGatt?.disconnect()
                }
            } else {
                bleEndLifecycle()
            }
        }
    }

    private fun prepareAndStartBleScan() {
        ensureBluetoothCanBeUsed { isSuccess, message ->
            appendLog(message)
            if (isSuccess) {
                safeStartBleScan()
            }
        }
    }

    private fun safeStartBleScan() {
        if (isScanning) {
            appendLog("Already scanning")
            return
        }

        val serviceFilter = scanFilter.serviceUuid?.uuid.toString()
        appendLog("Starting BLE scan, filter: $serviceFilter")

        isScanning = true
        lifecycleState = BLELifecycleState.Scanning
        bleScanner.startScan(mutableListOf(scanFilter), scanSettings, scanCallback)
    }

    private fun safeStopBleScan() {
        if (!isScanning) {
            appendLog("Already stopped")
            return
        }

        appendLog("Stopping BLE scan")
        isScanning = false
        bleScanner.stopScan(scanCallback)
    }

    private fun subscribeToIndications(characteristic: BluetoothGattCharacteristic, gatt: BluetoothGatt) {
        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        appendLog("Subscribing to: " + characteristic.uuid.toString())

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                appendLog("ERROR: setNotification(true) failed for ${characteristic.uuid}")
                //return
            }
            cccDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccDescriptor)

        }
    }

    private fun unsubscribeFromCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val gatt = connectedGatt ?: return

        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, false)) {
                appendLog("ERROR: setNotification(false) failed for ${characteristic.uuid}")
                return
            }
            cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccDescriptor)
        }
    }



    //region BLE Scanning
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
        .build()

    private val scanSettings: ScanSettings
        get() {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                scanSettingsSinceM
            } else {
                scanSettingsBeforeM
            }
        }

    private val scanSettingsBeforeM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setReportDelay(0)
        .build()

    @RequiresApi(Build.VERSION_CODES.M)
    private val scanSettingsSinceM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name: String? = result.scanRecord?.deviceName ?: result.device.name
            appendLog("onScanResult name=$name address= ${result.device?.address}")
            safeStopBleScan()
            lifecycleState = BLELifecycleState.Connecting

            Timer().schedule(500) {
                result.device.connectGatt(this@UIActivity, true, gattCallback,2)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            appendLog("onBatchScanResults, ignoring")
        }

        override fun onScanFailed(errorCode: Int) {
            appendLog("onScanFailed errorCode=$errorCode")
            safeStopBleScan()
            lifecycleState = BLELifecycleState.Disconnected
            bleRestartLifecycle()
        }
    }
    //endregion

    //region BLE events, when connected
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // TODO: timeout timer: if this callback not called - disconnect(), wait 120ms, close()

            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    appendLog("Connected to $deviceAddress")

                    // TODO: bonding state

                    // recommended on UI thread https://punchthrough.com/android-ble-guide/
                    Handler(Looper.getMainLooper()).post {
                        lifecycleState = BLELifecycleState.ConnectedDiscovering
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    appendLog("Disconnected from $deviceAddress")
                    setConnectedGattToNull()
                    gatt.close()
                    lifecycleState = BLELifecycleState.Disconnected
                    bleRestartLifecycle()
                }
            } else {
                // TODO: random error 133 - close() and try reconnect

                appendLog("ERROR: onConnectionStateChange status=$status deviceAddress=$deviceAddress, disconnecting")

                setConnectedGattToNull()
                gatt.close()
                lifecycleState = BLELifecycleState.Disconnected
                Timer().schedule(2000) {
                    bleRestartLifecycle()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            appendLog("onServicesDiscovered services.count=${gatt.services.size} status=$status")

            if (status == 129 /*GATT_INTERNAL_ERROR*/) {
                // it should be a rare case, this article recommends to disconnect:
                // https://medium.com/@martijn.van.welie/making-android-ble-work-part-2-47a3cdaade07
                appendLog("ERROR: status=129 (GATT_INTERNAL_ERROR), disconnecting")
                gatt.disconnect()
                return
            }

            val service = gatt.getService(UUID.fromString(SERVICE_UUID)) ?: run {
                appendLog("ERROR: Service not found $SERVICE_UUID, disconnecting")
                gatt.disconnect()
                return
            }

            connectedGatt = gatt
            characteristicForWrite = service.getCharacteristic(UUID.fromString(CHAR_FOR_WRITE_UUID))

            for(i in Notify_UUIDs.indices) {


                characteristicsForNotify[i] =
                    service.getCharacteristic(UUID.fromString(Notify_UUIDs[i]))

                characteristicsForNotify[i]?.let {
                    lifecycleState = BLELifecycleState.ConnectedSubscribing
                    Timer().schedule(50*i.toLong()+50) {
                        subscribeToIndications(it, gatt)
                    }
                } ?: run {
                    appendLog("WARN: characteristic not found $Notify_UUIDs[i]")
                    lifecycleState = BLELifecycleState.Connected
                }

            }
            for(i in characteristicsForRead.indices){
                characteristicsForRead[i] = service.getCharacteristic(UUID.fromString(Read_UUIDs[i]))
            }

            Thread {
                Timer().scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        readCharacteristics()
                    }
                }, 1000, 3000)
            }.start()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            //appendLog("READ EXECUTED")
            var found = false
            for (i in Read_UUIDs.indices){
                found = true
                if (characteristic.uuid == UUID.fromString(Read_UUIDs[i])) {
                    val strValue = characteristic.value
                    val buffer = ByteBuffer.wrap(strValue.reversedArray())
                    val asfloat = buffer.float
                    bluetoothdata[i+BT_NOTIFYS] = asfloat
                    //appendLog("Read this: " + asfloat)

                    /*val log = "onCharacteristicRead " + Read_UUIDs[i].toString() + "  "+ when (status)  {
                        BluetoothGatt.GATT_SUCCESS -> "OK, value=\"$asfloat\""
                        BluetoothGatt.GATT_READ_NOT_PERMITTED -> "not allowed"
                        else -> "error $status"
                    }
                    appendLog(log)*/
                    //runOnUiThread {
                        //CalculateData(bluetoothdata)
                        //update on screen data!!
                        //textViewReadValue.text = strValue
                    //}
                }
            }
            if (!found){
                appendLog("onCharacteristicRead unknown uuid $characteristic.uuid")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (characteristic.uuid == UUID.fromString(CHAR_FOR_WRITE_UUID)) {
                val log: String = "onCharacteristicWrite " + when (status) {
                    BluetoothGatt.GATT_SUCCESS -> "OK"
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "not allowed"
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "invalid length"
                    else -> "error $status"
                }
                appendLog(log)
            } else {
                appendLog("onCharacteristicWrite unknown uuid $characteristic.uuid")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            var found = false
            for (i in Notify_UUIDs.indices){
                if (characteristic.uuid == UUID.fromString(Notify_UUIDs[i])) {
                    found = true
                    val strValue = characteristic.value


                    val buffer = ByteBuffer.wrap(strValue.reversedArray())
                    val asfloat = buffer.float
                    bluetoothdata[i] = asfloat
                    //appendLog("\"$asfloat\" at \"$i\"")
                    //textViewIndicateValue.text = asfloat.toString()

                }
            }
            if (!found){
                appendLog("onCharacteristicChanged unknown uuid $characteristic.uuid")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            var found = false
            appendLog("Descriptor write to: " + descriptor.characteristic.uuid.toString())

            for (i in Notify_UUIDs.indices){
                if (descriptor.characteristic.uuid == UUID.fromString(Notify_UUIDs[i])) {
                    found = true
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val value = descriptor.value
                        val isSubscribed = value.isNotEmpty() && value[0].toInt() != 0
                        //val subscriptionText = when (isSubscribed) {
                        //true -> getString(R.string.text_subscribed)
                        //false -> getString(R.string.text_not_subscribed)
                        //}
                        //appendLog("onDescriptorWrite $subscriptionText")
                        //runOnUiThread {
                        //}
                    } else {
                        appendLog("ERROR: onDescriptorWrite status=$status uuid=${descriptor.uuid} char=${descriptor.characteristic.uuid}")
                    }

                    // subscription processed, consider connection is ready for use
                    lifecycleState = BLELifecycleState.Connected
                }
            }
            if (!found){
                appendLog("onDescriptorWrite unknown uuid $descriptor.characteristic.uuid")
            }
        }
    }
    //endregion

    //region BluetoothGattCharacteristic extension
    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWriteable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWriteableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return (properties and property) != 0
    }
    //endregion

    //region Permissions and Settings management
    enum class AskType {
        AskOnce,
        InsistUntilSuccess
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultHandlers[requestCode]?.let { handler ->
            handler(resultCode)
        } ?: runOnUiThread {
            appendLog("ERROR: onActivityResult requestCode=$requestCode result=$resultCode not handled")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResultHandlers[requestCode]?.let { handler ->
            handler(permissions, grantResults)
        } ?: runOnUiThread {
            appendLog("ERROR: onRequestPermissionsResult requestCode=$requestCode not handled")
        }
    }

    private fun ensureBluetoothCanBeUsed(completion: (Boolean, String) -> Unit) {
        grantBluetoothCentralPermissions(AskType.AskOnce) { isGranted ->
            if (!isGranted) {
                completion(false, "Bluetooth permissions denied")
                return@grantBluetoothCentralPermissions
            }

            enableBluetooth(AskType.AskOnce) { isEnabled ->
                if (!isEnabled) {
                    completion(false, "Bluetooth OFF")
                    return@enableBluetooth
                }

                grantLocationPermissionIfRequired(AskType.AskOnce) { isGranted ->
                    if (!isGranted) {
                        completion(false, "Location permission denied")
                        return@grantLocationPermissionIfRequired
                    }

                    completion(true, "Bluetooth ON, permissions OK, ready")
                }
            }
        }
    }

    private fun enableBluetooth(askType: AskType, completion: (Boolean) -> Unit) {
        if (bluetoothAdapter.isEnabled) {
            completion(true)
        } else {
            val intentString = BluetoothAdapter.ACTION_REQUEST_ENABLE
            val requestCode = ENABLE_BLUETOOTH_REQUEST_CODE

            // set activity result handler
            activityResultHandlers[requestCode] = { result -> Unit
                val isSuccess = result == Activity.RESULT_OK
                if (isSuccess || askType != AskType.InsistUntilSuccess) {
                    activityResultHandlers.remove(requestCode)
                    completion(isSuccess)
                } else {
                    // start activity for the request again
                    //startActivityForResult(Intent(intentString), requestCode)
                }
            }

            // start activity for the request
            //startActivityForResult(Intent(intentString), requestCode)
        }
    }

    private fun grantLocationPermissionIfRequired(askType: AskType, completion: (Boolean) -> Unit) {
        val wantedPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // BLUETOOTH_SCAN permission has flag "neverForLocation", so location not needed
            completion(true)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasPermissions(wantedPermissions)) {
            completion(true)
        } else {
            runOnUiThread {
                val requestCode = LOCATION_PERMISSION_REQUEST_CODE

                // prepare motivation message
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Location permission required")
                builder.setMessage("BLE advertising requires location access, starting from Android 6.0")
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    requestPermissionArray(wantedPermissions, requestCode)
                }
                builder.setCancelable(false)

                // set permission result handler
                permissionResultHandlers[requestCode] = { permissions, grantResults ->
                    val isSuccess = grantResults.firstOrNull() != PackageManager.PERMISSION_DENIED
                    if (isSuccess || askType != AskType.InsistUntilSuccess) {
                        permissionResultHandlers.remove(requestCode)
                        completion(isSuccess)
                    } else {
                        // show motivation message again
                        builder.create().show()
                    }
                }

                // show motivation message
                builder.create().show()
            }
        }
    }

    private fun grantBluetoothCentralPermissions(askType: AskType, completion: (Boolean) -> Unit) {
        val wantedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            )
        } else {
            emptyArray()
        }

        if (wantedPermissions.isEmpty() || hasPermissions(wantedPermissions)) {
            completion(true)
        } else {
            runOnUiThread {
                val requestCode = BLUETOOTH_ALL_PERMISSIONS_REQUEST_CODE

                // set permission result handler
                permissionResultHandlers[requestCode] = { _ /*permissions*/, grantResults ->
                    val isSuccess = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                    if (isSuccess || askType != AskType.InsistUntilSuccess) {
                        permissionResultHandlers.remove(requestCode)
                        completion(isSuccess)
                    } else {
                        // request again
                        requestPermissionArray(wantedPermissions, requestCode)
                    }
                }

                requestPermissionArray(wantedPermissions, requestCode)
            }
        }
    }

    private fun Context.hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermissionArray(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
    //endregion
}