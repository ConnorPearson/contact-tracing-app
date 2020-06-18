package com.example.contacttracingapp;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class serverComms {
    static String postData(final String URL, final JSONObject write) throws InterruptedException {
        final Boolean[] connectionSuccess = new Boolean[1];
        final String[] read = new String[1];

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    //Setup connection preferences
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());

                    os.writeBytes(write.toString());

                    os.flush();
                    os.close();

                    InputStreamReader input = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(input);

                    //If status has changed and status is RED, post UUIDs of potentially exposed users
                    if (connection.getResponseCode() == 200) {
                        connectionSuccess[0] = true;
                        read[0] = bufferedReader.readLine();
                    }
                    else {
                        throw new IOException();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();

        if (connectionSuccess[0])
            return read[0];

        return null;
    }

}
