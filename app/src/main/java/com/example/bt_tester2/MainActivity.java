package com.example.bt_tester2;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter aAdapter;
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket btSocket;


    ArrayList<String> mac_addresses = new ArrayList<String>();
    ArrayList<String> device_names = new ArrayList<String>();



    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final String MAC_SAVE = "a1b2";
    static final String DEVICE_SAVE = "a1b1";


    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        sharedPref = super.getPreferences(Context.MODE_PRIVATE);

        String savedmac = sharedPref.getString(MAC_SAVE, "");
        String saveddevice = sharedPref.getString(DEVICE_SAVE, "");
        if (!savedmac.equals("")){
            StartUIActivity(saveddevice,savedmac);
        }
        System.out.println(savedmac);


        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            ArrayList<String> list = new ArrayList<String>();

            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list.add(deviceName+"\n"+deviceHardwareAddress);
                mac_addresses.add(deviceHardwareAddress);
                device_names.add(deviceName);
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.customlist, R.id.textviewer1, list);
            listView.setAdapter(adapter);
        }

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                String devicename = device_names.get(position).toString();
                String mac = mac_addresses.get(position).toString();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(MAC_SAVE,mac);
                editor.putString(DEVICE_SAVE,devicename);
                editor.apply();
                System.out.println(devicename + "devicename and " + mac + " mac saved");

                StartUIActivity(devicename,mac);

            }
        });

    }

    void StartUIActivity(String devicename, String mac){
        Intent intent = new Intent(listView.getContext(), UIActivity.class);
        intent.putExtra("device", devicename);
        intent.putExtra("mac",mac);
        startActivity(intent);
    }




}




