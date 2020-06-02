package com.example.contacttracingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReportSymptoms extends AppCompatActivity {
    private int checkedElements = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_symptoms);
    }

    public void logCheckedElement(View view) {
        if (((CheckBox)view).isChecked())
            checkedElements++;
        else
            checkedElements--;

        if (checkedElements > 0)
            (findViewById(R.id.submitSymptomsBtn)).setEnabled(true);
        else
            findViewById(R.id.submitSymptomsBtn).setEnabled(false);
    }

    public void submitSymptoms(View view) throws InterruptedException {
        final boolean[] connectionSuccess = new boolean[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://192.168.0.90:3000/receiveSymptomaticData");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept","application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());

                    os.writeBytes(createSymptomsJson().toString());

                    os.flush();
                    os.close();

                     if(connection.getResponseCode() == 200) {
                         connectionSuccess[0] = true;
                     }
                     else {
                         connectionSuccess[0] = false;
                         throw new Exception();
                     }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();

        if (connectionSuccess[0]){
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newStatus",  "RED");
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else {
            CharSequence text = "Connection error occurred! Please make sure you have an active internet connection, then try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
    }

    private JSONObject createSymptomsJson() {
        JSONObject symptoms = new JSONObject();

        try {
            symptoms.put("fever", ((CheckBox)findViewById(R.id.feverCheckBox)).isChecked());
            symptoms.put("fatigue", ((CheckBox)findViewById(R.id.fatigueCheckBox)).isChecked());
            symptoms.put("cough",((CheckBox) findViewById(R.id.dryCoughCheckBox)).isChecked());
            symptoms.put("lossOfAppetite", ((CheckBox)findViewById(R.id.lossOfAppetiteCheckBox)).isChecked());
            symptoms.put("bodyAche", ((CheckBox)findViewById(R.id.bodyAchesCheckBox)).isChecked());
            symptoms.put("breathShortness", ((CheckBox)findViewById(R.id.breathShortnessCheckBox)).isChecked());
            symptoms.put("mucusOrPhlegm", ((CheckBox) findViewById(R.id.mucusPhlegmCheckBox)).isChecked());
            symptoms.put("soreThroat", ((CheckBox)findViewById(R.id.soreThroatCheckBox)).isChecked());
            symptoms.put("headaches",((CheckBox) findViewById(R.id.headachesCheckBox)).isChecked());
            symptoms.put("chillsAndShaking",((CheckBox) findViewById(R.id.chillsCheckBox)).isChecked());
            symptoms.put("additional",((TextView) findViewById(R.id.additionalTextBox)).getText());
        } catch (Exception e) {
            Log.e("JSON Symptom Parse", e.toString());
        }

        return symptoms;
    }
}
