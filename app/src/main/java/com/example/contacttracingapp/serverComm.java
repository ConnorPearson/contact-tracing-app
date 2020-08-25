package com.example.contacttracingapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <h1>serverComm.java</h1>
 * This class is used to post data to a remote server.
 * <p>
 * This class is used across multiple classes and was therefore given its own class to prevent
 * repeating code.
 *
 * @author  Connor Pearson
 * @since 2020-05-18
 */

public class serverComm {

    /**
     * Method requires a String containing a valid URL for the location the data is being sent to
     * and a String containing the JSON formatted write data.
     * <p>
     * Method can throw an IOException, InterruptedException or a MalformedURLException.
     */
    static String postData(final String URL, final String write) throws InterruptedException {
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

                    os.writeBytes(write);

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
                    connectionSuccess[0] = false;
                }
            }
        });

        //Begin thread then once complete execute if connectionSuccess check.
        thread.start();
        thread.join();

        if (connectionSuccess[0])
            return read[0];

        return null;
    }

}
