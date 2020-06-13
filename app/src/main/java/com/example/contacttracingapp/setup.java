package com.example.contacttracingapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;


public class setup extends AppCompatActivity {

    private static final int LOCATION_PERMISSION = 42;
    private static final int BLUETOOTH_PERMISSION = 43;
    private static final int BLUETOOTH_ADMIN_PERMISSION = 44;
    private static final int FINE_LOCATION_PERMISSION = 11;

     public void agreementBtnCheck(View view) {
         if (((CheckBox)view).isChecked()) {
             findViewById(R.id.submitBtn).setEnabled(true);
         }
         else {
             findViewById(R.id.submitBtn).setEnabled(false);
         }
     }

    public void checkPermissions(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(setup.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(setup.this, new String[] { permission }, requestCode);
        }
    }

     private JSONObject createUserDataJson() {
         JSONObject userData = new JSONObject();

         try {
             userData.put("firstName", ((TextView) findViewById(R.id.firstNameTxt)).getText());
             userData.put("surname", ((TextView) findViewById(R.id.surnameTxt)).getText());
             userData.put("address", ((TextView) findViewById(R.id.addressTxt)).getText());
             userData.put("passportID", ((TextView) findViewById(R.id.passportIDTxt)).getText());
             userData.put("uuid", UUID.randomUUID().toString());

         } catch (JSONException e) {
             e.printStackTrace();
         }

         return userData;
     }

     public void closeSetup(View view) {
         Context context = getApplicationContext();

         if (checkFieldsPopulated()) {
             getSharedPreferences("isSetupComplete", MODE_PRIVATE).edit().putBoolean("isSetupComplete", false).apply();

             Intent resultIntent = new Intent();
             resultIntent.putExtra("userdata",  createUserDataJson().toString());
             setResult(RESULT_OK, resultIntent);

             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);

             //Write user data json to application directory
             writeToFile(createUserDataJson().toString());

             finish();

             serviceReceiver receiver = new serviceReceiver();
             receiver.onReceive(getApplicationContext(), getIntent());
         }
         else {
             CharSequence text = "Some fields appear to be empty, Please enter the missing info to submit.";
             int duration = Toast.LENGTH_LONG;

             Toast toast = Toast.makeText(context, text, duration);
             toast.show();
         }
     }

    private void writeToFile(String data) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir() + "/" + "userData.json"));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
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

        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);

        checkPermissions("android.permission.ACCESS_FINE_LOCATION", FINE_LOCATION_PERMISSION);
        checkPermissions("android.permission.ACCESS_COARSE_LOCATION", LOCATION_PERMISSION);
        checkPermissions("android.permission.BLUETOOTH", BLUETOOTH_PERMISSION);
        checkPermissions("android.permission.BLUETOOTH_ADMIN", BLUETOOTH_ADMIN_PERMISSION);

        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                Toast.makeText(this, "Your device does not support the features required for this application.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enable bluetooth!", Toast.LENGTH_SHORT).show();
        }
    }
}
