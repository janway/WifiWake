package com.oncogene.wifiwake;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private FileOutputStream outputStream = null;
    private PowerManager.WakeLock mWakelock;
    private PrintWriter mPrintWriter = null;
    //
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                outputStream.write((new Date().toString() + ((String) msg.obj) + "  savelocal\n")
                        .getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
//            releaseWakeLock();
        }
    };
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        File dir = this.getFilesDir();
        File outFile = new File(dir, "test.txt");
        writeToFile(outFile, "Hello!");
        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                //File file = new File("/sdcard/testlog-lock.txt");
                /*
                File file = new File("wifiwake.txt");
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                Log.d("test","massage");
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                }
                */
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("192.168.0.104", 4005));
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader inputStream2 = new BufferedReader(new InputStreamReader(
                            inputStream));
                    String lineString;
                    while ((lineString = inputStream2.readLine()) != null) {
//                        acquireWakeLock();
                        outputStream.write((new Date().toString() + lineString + " receive\n")
                                .getBytes());
                        Message msgMessage = handler.obtainMessage(1, lineString);
                        handler.sendMessageDelayed(msgMessage, 5000);
                    }
                } catch (UnknownHostException e) {
                    try {
                        outputStream.write(e.getMessage().getBytes());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    try {
                        outputStream.write(e.getMessage().getBytes());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }
    //
    private void writeToFile(File fout, String data) {
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
            ;
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
                ;
            }
        }
    }
    //
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (mWakelock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
        }
        mWakelock.acquire();
    }
    private void releaseWakeLock() {
        if (mWakelock != null && mWakelock.isHeld()) {
            mWakelock.release();
        }
        mWakelock = null;
    }
}