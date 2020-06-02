package com.example.contacttracingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;


public class setup extends AppCompatActivity {

     public void agreementBtnCheck(View view) {
         if (((CheckBox)view).isChecked()) {
             findViewById(R.id.submitBtn).setEnabled(true);
         }
         else {
             findViewById(R.id.submitBtn).setEnabled(false);
         }
     }

     private JSONObject createUserDataJson() {
         JSONObject userData = new JSONObject();

         try {
             userData.put("firstName", ((TextView) findViewById(R.id.firstNameTxt)).getText());
             userData.put("surname", ((TextView) findViewById(R.id.surnameTxt)).getText());
             userData.put("address", ((TextView) findViewById(R.id.addressTxt)).getText());
             userData.put("passportID", ((TextView) findViewById(R.id.passportIDTxt)).getText());

         } catch (JSONException e) {
             e.printStackTrace();
         }

         return userData;
     }

     public void closeSetup(View view) {
         Context context = getApplicationContext();

         if (checkFieldsPopulated()) {
             getSharedPreferences("isSetupComplete", MODE_PRIVATE).edit().putBoolean("isSetupComplete", false).apply();

             OutputStreamWriter writer;

             try {
                 writer = new OutputStreamWriter(context.openFileOutput("userData.json", Context.MODE_PRIVATE));
                 writer.write(createUserDataJson().toString());
                 writer.close();

             } catch (Exception e) {
                 Log.e("User data write", e.toString());
             }

             Intent resultIntent = new Intent();
             resultIntent.putExtra("userdata",  createUserDataJson().toString());
             setResult(RESULT_OK, resultIntent);

             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);

             finish();
         }
         else {
             CharSequence text = "Some fields appear to be empty, Please enter the missing info to submit.";
             int duration = Toast.LENGTH_LONG;

             Toast toast = Toast.makeText(context, text, duration);
             toast.show();
         }
     }

     private boolean checkFieldsPopulated() {
         return     ((TextView)findViewById(R.id.firstNameTxt)).length() > 0 &
                        ((TextView)findViewById(R.id.surnameTxt)).length() > 0 &
                        ((TextView)findViewById(R.id.addressTxt)).length() > 0 &
                         ((TextView)findViewById(R.id.passportIDTxt)).length() > 0;
     }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }
}
