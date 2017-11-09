package com.example.bingkel.logsrecord;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zexings on 8/22/14.
 */
public class LogcatRecorder {
    private static final String TAG = "logcat-recorder";
    private static LogcatRecorder sInstance;
    public static LogcatRecorder getInstance() {
        if (sInstance == null) {
            synchronized (LogcatRecorder.class) {
                if (sInstance == null) {
                    sInstance = new LogcatRecorder();
                }
            }
        }
        return sInstance;
    }

    public void record() {
        synchronized (this) {
            if (running == true) {
                Log.d(TAG, "record thread is running. skip duplicate record");
                return;
            }
            running = true;
        }
        Log.i(TAG, "start thread to record logcat");
        new Thread(new RecordJob()).start();
    }

    public void stop() {
        running = false;
        Log.e(TAG, "stop called, running = false", new Throwable());
    }

    Process p;
    private volatile boolean running = false;

    // zexings: not working
    private void recording0() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Log.d(TAG, "date : " + sdf.format(d));
        String filename = "/sdcard/phone-" + sdf.format(d) + ".log";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat -v threadtime 2>&1 -f " + filename);
//            process = Runtime.getRuntime().exec("logcat -v threadtime 2>&1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process == null) {
            Log.e(TAG, "logcat process is null");
            return;
        }

        while(running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "destroy reading process");
        process.destroy();
    }

    private void recording() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Log.d(TAG, "date : " + sdf.format(d));
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.i(TAG, "sdcard dir = " + sdcard);
        Log.i(TAG, "path separator = " + File.pathSeparator);
        Log.i(TAG, "separator = " + File.separator);
        String filename = sdcard + File.separator + "phone-" + sdf.format(d) + ".log";
        Log.i(TAG, "logcat recroding to : " + filename);
//        String filename = "/sdcard/phone-" + sdf.format(d) + ".log";
        Process process = null;
        try {
//                Process process = Runtime.getRuntime().exec("logcat -v threadtime 2>&1 >/sdcard/phone-" + sdf.format(d) + ".log");
            Log.i(TAG, "run logcat -v threadtime 2>&1");
            process = Runtime.getRuntime().exec("logcat -v threadtime 2>&1");// Only this field
        } catch (IOException e) {
            Log.i(TAG, "run logcat -v threadtime 2>&1 throw exception");
            e.printStackTrace();
        }
        if (process == null) {
            Log.e(TAG, "logcat process is null");
            return;
        }

        BufferedReader br = null;
        FileOutputStream fos = null;
        try {
            Log.i(TAG, "open " + filename);
            fos = new FileOutputStream(filename);
            Log.i(TAG, "get input stream for logcat recording");
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while(running) {
                line = br.readLine() + "\n";
                fos.write(line.getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "destroy reading process");
            process.destroy();
        }
    }

    public class RecordJob implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "run record job for logcat");
            recording();
        }
    }
}