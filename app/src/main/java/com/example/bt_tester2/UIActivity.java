package com.example.bt_tester2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class UIActivity extends AppCompatActivity {


    final byte[] hellomsg = {'s', 't', 'a', 'r', 't', '\n'};


    final int datapoints=13;
    float[] data = new float[13];

    byte[] rawdata = null;

    class Data{
        float tempMotor, motorCurr, batCurr, kmh, inpVolt, ampHours, ampHoursC, km, wattHours, wattHoursC, totalWh, totalWhC, totalkm, power=0, maxpower=0, minpower=0, percent=0;
    }

    int draw_counter = 0;

    Data btdata = new Data();

    ProgressBar batbar = null;
    TextView kmh = null;
    TextView Voltage = null;
    TextView Amps = null;
    TextView Distance = null;
    TextView TotalDistance = null;
    TextView MotorTemp = null;
    TextView Energy = null;
    TextView EnergyCharged = null;
    TextView TotalEnergy = null;
    TextView TotalEnergyCharged = null;
    TextView Power = null;

    String devicename = null;
    String mac = null;

    int getPower(){
        int returnvalue=0;
        if (btdata.power>0) returnvalue= (int) (btdata.power * (100.0f/btdata.maxpower));
        if (btdata.power<0) returnvalue= (int) (btdata.power * (-100.0f/btdata.minpower));
        if (returnvalue < -100) returnvalue = -100;
        if (returnvalue > 100) returnvalue = 100;

        return returnvalue;
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    //Bluetooth Message Handling
    BluetoothHandler btHandler = new BluetoothHandler(new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);


            switch (msg.what){
                case 1:
                    rawdata = (byte[])msg.obj;
                    CalculateData(rawdata);
                    if (draw_counter %5 == 0) {
                        DisplayData(rawdata);
                        draw_counter =0;
                    }
                    draw_counter++;
                    break;
                case 2:
                    ShowConnectStatus((byte[]) msg.obj);
                    break;
            }


        }
    });
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uiactivity);



        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();

                            btHandler.write(intent.getStringExtra("settings").getBytes());


                            System.out.println("INTENT BEKOMMEN");
                        }
                    }
                });



        final ImageButton Settingsbutton = findViewById(R.id.settingsbutton);
        Settingsbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settingsbutton.getContext(),SettingsActivity.class);

                //startActivity(intent);
                mStartForResult.launch(new Intent(Settingsbutton.getContext(), SettingsActivity.class));


            }
        });

        batbar =                findViewById(R.id.batterybar);
        kmh =                   findViewById(R.id.speedtext);
        Voltage =               findViewById(R.id.textVoltage);
        Amps =                  findViewById(R.id.textAmps);
        Distance =              findViewById(R.id.textDistance);
        TotalDistance =         findViewById(R.id.textTotalDistance);
        MotorTemp =             findViewById(R.id.textMotorTemp);
        Energy =                findViewById(R.id.textEnergy);
        EnergyCharged =         findViewById(R.id.textEnergyCharged);
        TotalEnergy =           findViewById(R.id.textTotalEnergy);
        TotalEnergyCharged =    findViewById(R.id.textTotalEnergyCharged);
        Power =                 findViewById(R.id.textPower);



        Intent intent = getIntent();


            devicename = intent.getStringExtra("device");
            mac = intent.getStringExtra("mac");
            System.out.println("bt connected");
            ConnectBT();




        System.out.println("page2");

        Switch mySwitch = (Switch) findViewById(R.id.enableswitch);
        if (mySwitch.isChecked()){

        }

        StartDrawing();




//        new Thread(new Runnable() {
//            Timer timer2 = new Timer();
//            int delay = 10;
//            int period = 250;
//
//            @Override
//            public void run() {
//                timer2.scheduleAtFixedRate(new TimerTask() {
//
//                    public void run() {
//                        DisplayData(rawdata);
//                    }
//
//                }, delay, period);
//
//            }
//        }).start();





    }

    void StartDrawing(){
        GraphView myview =      findViewById(R.id.relativeLayout);
        myview.invalidate();


        new Thread(new Runnable() {
            Timer timer = new Timer();
            int delay = 10;
            int period = 50;

            @Override
            public void run() {
                timer.scheduleAtFixedRate(new TimerTask() {

                    public void run() {
                        myview.setData(getPower());
                        myview.invalidate();

                    }

                }, delay, period);
                System.out.println("RUNNING");
            }
        }).start();
    }

    void ConnectBT(){


        String text = null;
        text = "Connecting to " + devicename;
        Toast toast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG);
        toast.show();


                BluetoothDevice esp32 = btAdapter.getRemoteDevice(mac);
                Boolean connected = true;
                btHandler.connect(esp32);

    }

    void CalculateData(byte message[]){
        //Extract data into Floats
        for(int a = 0; a<datapoints; a++){

            int inp = (0xFF & message[a*5+1]) | ((0xFF & message[a*5+2]) << 8) |
                    ((0xFF & message[a*5+3]) << 16) | ((0xFF & message[a*5+4]) << 24);
            float asFloat = Float.intBitsToFloat(inp);
            data[a] = asFloat;
        }
        int bits = Float.floatToIntBits(data[4]);

        //Write data into correct containers
        btdata.tempMotor    = data[0];
        btdata.motorCurr    = data[1];
        btdata.batCurr      = data[2];
        btdata.kmh          = data[3];
        btdata.inpVolt      = data[4];
        btdata.ampHours     = data[5];
        btdata.ampHoursC    = data[6];
        btdata.km           = data[7];
        btdata.wattHours    = data[8];
        btdata.wattHoursC   = data[9];
        btdata.totalWh      = data[10];
        btdata.totalWhC     = data[11];
        btdata.totalkm      = data[12];


        //Temp Config PLS DELETE LATER
        int cells_s = 12;
        float batt_low = 3.0f, batt_high = 4.2f;

        float percent = (btdata.inpVolt-(batt_low*(float)cells_s)) * ((100.0f/((batt_high-batt_low)*(float)cells_s)));
        if (percent > 100) percent = 100;
        if (percent < 0) percent = 0;
        btdata.percent = percent;
        btdata.power = btdata.batCurr * btdata.inpVolt;
        if (btdata.power > btdata.maxpower) btdata.maxpower = btdata.power;
        if (btdata.power < btdata.minpower) btdata.minpower = btdata.power;
    }



    void DisplayData(byte message[]){

        batbar.setProgress((int) btdata.percent);
        Voltage.setText("Battery Voltage: " + btdata.inpVolt + "V");
        kmh.setText(round(btdata.kmh,1)+"");
        Power.setText("Power: " + round(btdata.power,1) + "W");
        Amps.setText("Battery Amps: " + round(btdata.batCurr,1) + "A");
        Distance.setText("Distance: " + round(btdata.km,2) + "Km");
        TotalDistance.setText("Total Distance: " + round(btdata.totalkm,1) + "km");
        MotorTemp.setText("Motor Temp: " + round(btdata.tempMotor,1) + "°C");
        Energy.setText("Energy Used: " + round(btdata.wattHours,2) + "Wh");
        EnergyCharged.setText("Energy regen: " + round(btdata.wattHoursC,2) + "Wh");
        TotalEnergy.setText("Total Energy used: " + round(btdata.totalWh,1) + "Wh");
        TotalEnergyCharged.setText("Total Energy regen: " + round(btdata.totalWhC,1) + "Wh");
    }



void ShowConnectStatus(byte[] connected){

    String text2 = null;
    Toast toast2 = Toast.makeText(getApplicationContext(),text2,Toast.LENGTH_LONG);

    if(connected[0] == 0x01) {
        text2 = "Connection to " + devicename + " suceeded!";
        StartDataRead();

    } else
        text2 = "Connection to " + devicename + " failed!";

    toast2 = Toast.makeText(getApplicationContext(),text2,Toast.LENGTH_LONG);
    toast2.show();
}

    void StartDataRead(){
        System.out.println("data read started");
        btHandler.write(hellomsg);

        btHandler.read();
    }




}

