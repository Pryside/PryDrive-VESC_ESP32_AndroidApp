package com.example.bt_tester2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothHandler{
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static BluetoothSocket btSocket = null;
    static BluetoothDevice esp32;
    // final char endofmsg[] = ("ENDOF\n"+0).toCharArray();
    final Byte[] endofmsg = {'E', 'N', 'D', 'O', 'F', '\n'};    //this terminates the message
    final int messagelen = 75;                                  //to check if full message came
    final int enofmsglen = endofmsg.length-1;

    Handler handler;
    public BluetoothHandler(Handler temp_handler){
        handler = temp_handler;
    }
    Handler connect_handler;


    InputStream inputStream = null;




    void connect(BluetoothDevice esp32){
        new Thread(new Runnable() {
            public void run() {
        int counter = 0;
        do {
            try {
                btSocket = esp32.createRfcommSocketToServiceRecord(mUUID);
                System.out.println(btSocket);
                btSocket.connect();
                System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while (!btSocket.isConnected() && counter < 3);


                Message msg = Message.obtain();
                byte[] Buffer = new byte[messagelen+1];
                if(btSocket.isConnected()) Buffer[0] = 0x01;
                else Buffer[0] = 0x00;
                msg.what = 2;
                msg.obj = Arrays.copyOfRange(Buffer, 0, 1);




                //inputStream.reset();
                msg.setTarget(handler);
                msg.sendToTarget();

            }
        }).start();
    }


    void write(byte[] msg){
        OutputStream outputStream = null;

        try {
            outputStream = btSocket.getOutputStream();
            outputStream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR WRITING DATA!");
        }
    }


    void read(){
        //InputStream inputStream = null;


        new Thread(new Runnable() {
            public void run() {

                byte[] Buffer = new byte[messagelen+1];
                int i = 0;

                try {
                    inputStream = btSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("ERROR BTSOCKET DEAD");
                }


                while(true) {
                    try {
                        //System.out.println(inputStream.available());
                        if (inputStream.available()>0){

                            //System.out.print("input: ");
                            Buffer[i] = (byte) inputStream.read();
                            //System.out.println(((int) Buffer[i]));


                            boolean bufferend = false;
                            if(i>7)//ADD ACUTAL LENGTH OF FULL STRING!
                                {
                                for (int u = enofmsglen; u >= 0; u--) {
                                    bufferend = true;
                                    if (Buffer[i +u -enofmsglen] != endofmsg[u]) {
                                        bufferend = false;
                                        break;
                                    }
                                }

                            }
                            

                            if(bufferend & i == messagelen) {

                                    Message msg = Message.obtain();
                                    msg.what = 1;
                                    msg.obj = Arrays.copyOfRange(Buffer, 0, i);


                                    //inputStream.reset();
                                    msg.setTarget(handler);
                                    msg.sendToTarget();
                                    Buffer = new byte[messagelen+1];
                                    i=0;
                                    //return;
                            }
                            else{

                                i++;
                            }

                            if(i>messagelen) return;
                        }

                    } catch (IOException e) {
                        System.out.println("ERROR INPUT STREAM READ");
                        System.out.println(e);
                        e.printStackTrace();
                        break;
                    }
                }


            }
        }).start();


    }

}