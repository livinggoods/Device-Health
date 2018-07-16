package org.goods.living.tech.health.device.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WriteToLogUtil {

    final static String TAG = WriteToLogUtil.class.getSimpleName();//BaseService.class.getSimpleName();


    public String logName;
    String text;
    File root = new File(Environment.getExternalStorageDirectory(), "LG/DEVICEHEALTH");

    static WriteToLogUtil writeToLogUtil;

    private WriteToLogUtil() {
        super();
        this.logName = "ussd_app_log.txt";


    }

    public static WriteToLogUtil getInstance() {

        if (writeToLogUtil == null) {
            writeToLogUtil = new WriteToLogUtil();
        }

        return writeToLogUtil;
    }

    public void clearLog() throws IOException {
        File gpxfile = new File(root, logName);
        if (gpxfile.exists()) {
            File renameto = new File(root, "log " + getDate() + ".txt");
            gpxfile.delete();
            gpxfile = new File(root, logName);
            gpxfile.createNewFile();
        } else {
            Log.i("Error", "file does not exist");
        }
    }

    public void log(String sBody) {
        try {

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, logName);
            if (gpxfile.exists()) {
                if (gpxfile.length() > 10000) {
                    //File renameto=new File(root,"log "+getDate()+".txt");
                    gpxfile.delete();
                    gpxfile = new File(root, logName);
                }
                FileWriter writer = new FileWriter(gpxfile, true);
                writer.append(getDate() + " - " + sBody + "\n");
                writer.flush();
                writer.close();
            } else {
                FileWriter writer = new FileWriter(gpxfile);
                BufferedWriter out = new BufferedWriter(writer);
                out.append(sBody);
                out.newLine();
                out.flush();
                out.close();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // importError = e.getMessage();
            //  iError();
        }
    }

    public ArrayList<String> readFromFile() {

        ArrayList<String> ret = new ArrayList<String>();

        // ArrayList<String> sInfo = new ArrayList<String>();

        String txtName = logName;
        File path = new File(root, txtName);

        try {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(path)));
            String line;

            while ((line = br.readLine()) != null) {

                Log.d("message", line);
                ret.add(line);

            }

            br.close();
        } catch (FileNotFoundException e) {

            System.err.println("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ret;
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
