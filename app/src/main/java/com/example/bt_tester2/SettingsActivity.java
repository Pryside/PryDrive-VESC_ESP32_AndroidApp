package com.example.bt_tester2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    final String endofsend = "set\n";
    static final String SETTINGS_CHK = "a1set00";
    static final String set_checkword = "this is set";


    static final String SETTINGS_sw1 = "a1sw1";
    static final String SETTINGS_sw2 = "a1sw2";
    static final String SETTINGS_rd1 = "a1srd1";
    static final String SETTINGS_rd2 = "a1srd2";
    static final String SETTINGS_rd3 = "a1srd3";
    static final String SETTINGS_rd4 = "a1srd4";



    SharedPreferences settingspref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch sw1 = findViewById(R.id.set1switch);
        Switch sw2 = findViewById(R.id.set2switch);
        RadioButton rd1 = findViewById(R.id.radioButton1);
        RadioButton rd2 = findViewById(R.id.radioButton2);
        RadioButton rd3 = findViewById(R.id.radioButton3);
        RadioButton rd4 = findViewById(R.id.radioButton4);
        RadioGroup rd = findViewById(R.id.radiogroup);


        settingspref = super.getPreferences(Context.MODE_PRIVATE);
        String check_settings = settingspref.getString(SETTINGS_CHK, "");
        if (check_settings.equals(set_checkword)){
            //checked if settings were saved, can be loaded
            sw1.setChecked(settingspref.getBoolean(SETTINGS_sw1,false));
            rd1.setChecked(settingspref.getBoolean(SETTINGS_rd1,false));
            rd2.setChecked(settingspref.getBoolean(SETTINGS_rd2,false));
            rd3.setChecked(settingspref.getBoolean(SETTINGS_rd3,false));
            rd4.setChecked(settingspref.getBoolean(SETTINGS_rd4,false));
            sw2.setChecked(settingspref.getBoolean(SETTINGS_sw2,false));
        }









        Button savebtn = (Button) findViewById(R.id.savebutton);
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String settings_data = "";

                settings_data += Integer.valueOf(boolToInt(sw1.isChecked())).toString();
                settings_data += Integer.valueOf((rd.indexOfChild(findViewById(rd.getCheckedRadioButtonId())))+1).toString();
                settings_data += Integer.valueOf(boolToInt(sw2.isChecked())).toString();
                settings_data += endofsend;

                Intent intent = new Intent(savebtn.getContext(),UIActivity.class);
                intent.putExtra("settings",settings_data);
                setResult(RESULT_OK, intent);
                finish();

                System.out.println("saved");


                SharedPreferences.Editor settingseditor = settingspref.edit();
                settingseditor.putBoolean(SETTINGS_sw1,sw1.isChecked());
                settingseditor.putBoolean(SETTINGS_rd1,rd1.isChecked());
                settingseditor.putBoolean(SETTINGS_rd2,rd2.isChecked());
                settingseditor.putBoolean(SETTINGS_rd3,rd3.isChecked());
                settingseditor.putBoolean(SETTINGS_rd4,rd4.isChecked());
                settingseditor.putBoolean(SETTINGS_sw2,sw2.isChecked());
                settingseditor.putString(SETTINGS_CHK,set_checkword);
                settingseditor.apply();
            }
        });

    }

    int boolToInt(boolean b) {
        return Boolean.compare(b, false);
    }


}