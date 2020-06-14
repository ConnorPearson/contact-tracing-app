package com.example.contacttracingapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class fileReadWrite {
    private static final String TAG = "fileReadWrite  class";

    static void writeToFile(String data, String filename, Context context) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(context.getFilesDir() + "/" + filename));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    static String loadFromFile(Context context, String filename) {
        String userData = null;

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString =  bufferedReader.readLine();
                inputStream.close();

                userData = receiveString;
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Cannot read file: " + e.toString());
        }

        return userData;
    }
}
