package com.example.contacttracingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static com.example.contacttracingapp.serverComm.postData;

public class ReportSymptoms extends AppCompatActivity {
    private int checkedElements = 0;
    private UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_symptoms);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        uuid = UUID.fromString(extras.getString("uuid"));
    }

    public void logCheckedElement(View view) {
        if (((CheckBox) view).isChecked())
            checkedElements++;
        else
            checkedElements--;

        if (checkedElements > 0)
            (findViewById(R.id.submitSymptomsBtn)).setEnabled(true);
        else
            findViewById(R.id.submitSymptomsBtn).setEnabled(false);
    }

    public void submitSymptoms(View view) throws InterruptedException {
        JSONObject proximityUUIDs = new JSONObject();

        try {
            proximityUUIDs = new JSONObject(fileReadWrite.loadFromFile(this, "proximityUuids.json"));
            proximityUUIDs.put(uuid.toString(), "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Send symptoms data
        postData("http://192.168.0.90:3000/receiveSymptomaticData", createSymptomsJson().toString());

        //Send uuids of previously close proximity devices
        postData("http://192.168.0.90:3000/receiveProximityUuids", proximityUUIDs.toString());

        Intent returnIntent = new Intent();
        returnIntent.putExtra("newStatus", "RED");
        setResult(2,returnIntent);
        finish();
    }

    private JSONObject createSymptomsJson() {
        JSONObject symptoms = new JSONObject();

        try {
            symptoms.put("fever", ((CheckBox) findViewById(R.id.feverCheckBox)).isChecked());
            symptoms.put("fatigue", ((CheckBox) findViewById(R.id.fatigueCheckBox)).isChecked());
            symptoms.put("cough", ((CheckBox) findViewById(R.id.dryCoughCheckBox)).isChecked());
            symptoms.put("lossOfAppetite", ((CheckBox) findViewById(R.id.lossOfAppetiteCheckBox)).isChecked());
            symptoms.put("bodyAche", ((CheckBox) findViewById(R.id.bodyAchesCheckBox)).isChecked());
            symptoms.put("breathShortness", ((CheckBox) findViewById(R.id.breathShortnessCheckBox)).isChecked());
            symptoms.put("mucusOrPhlegm", ((CheckBox) findViewById(R.id.mucusPhlegmCheckBox)).isChecked());
            symptoms.put("soreThroat", ((CheckBox) findViewById(R.id.soreThroatCheckBox)).isChecked());
            symptoms.put("headaches", ((CheckBox) findViewById(R.id.headachesCheckBox)).isChecked());
            symptoms.put("chillsAndShaking", ((CheckBox) findViewById(R.id.chillsCheckBox)).isChecked());
            symptoms.put("additional", ((TextView) findViewById(R.id.additionalTextBox)).getText());
        } catch (Exception e) {
            Log.e("JSON Symptom Parse", e.toString());
        }

        return symptoms;
    }
}
