package com.example.contacttracingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static JSONObject userData = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isSetupComplete = getSharedPreferences("isSetupComplete", MODE_PRIVATE).getBoolean("isSetupComplete", true);

        userData = loadUserData();

        setupPageAttributes();

        if (isSetupComplete) {
            Intent intent = new Intent(this, setup.class);
            startActivityForResult(intent, 1);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!(data == null)) {       //check data is not null
            switch (requestCode) {
                case 1:
                    try {
                        userData = new JSONObject(Objects.requireNonNull(data.getStringExtra("userdata")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    setupPageAttributes();

                    break;

                case 2:
                    String status = Objects.requireNonNull(data.getStringExtra("newStatus"));
                    changeStatus(status);
                    if (status.equals("RED"))
                        findViewById(R.id.reportSymptomsBtn).setEnabled(false);
            }
        }
    }

    public void openCovidWebPage(View view) {
        Uri govUrl = Uri.parse("https://www.gov.uk/coronavirus");
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, govUrl);
        startActivity(launchBrowser);
    }

    private JSONObject loadUserData() {
        JSONObject userData = null;

        try {
            InputStream inputStream = getApplicationContext().openFileInput("userData.json");

            if (inputStream != null) {
                InputStreamReader inputReader = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(inputReader);

                userData = new JSONObject(br.readLine());

                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userData;
    }

    public void openReportSymptoms(View view) {
        Intent intent = new Intent(this, ReportSymptoms.class);
        startActivityForResult(intent, 2);
    }

    public void setupPageAttributes() {
        ImageView QRCodeImg  = findViewById(R.id.QRCodeImg);

        try {
                ((TextView) findViewById(R.id.nameTxtView)).setText(getString(R.string.nameTxtView, userData.get("firstname"), userData.get("surname")));
                ((TextView) findViewById(R.id.addressTxtView)).setText(getString(R.string.addressTxtView, userData.get("address")));
                ((TextView) findViewById(R.id.passportNumTxtView)).setText(getString(R.string.passportNumTxtView, userData.get("passportid")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = TextToImageEncode(userData.toString());
            QRCodeImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    144, 144, null
            );
        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ?
                        ContextCompat.getColor(this, R.color.black):ContextCompat.getColor(this, R.color.white);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 144, 0, 0, bitMatrixWidth, bitMatrixHeight);

        return bitmap;
    }

    public void changeStatus(String color) {
        OutputStreamWriter writer;

        try {
            userData.put("status", color.toUpperCase());

            writer = new OutputStreamWriter(getApplicationContext().openFileOutput("userData.json", Context.MODE_PRIVATE));
            writer.write(userData.toString());
            writer.close();

        } catch (Exception e) {
            Log.e("User data write", e.toString());
        }

        applyStatus(color.toUpperCase());
    }

    private void applyStatus(String status){
        int statusColor;

        switch (status.toUpperCase()) {
            case "GREEN":
                statusColor = Color.parseColor("#59ff00");
                break;

            case "AMBER" :
                statusColor = Color.parseColor("#fceb05");
                break;

            default:    //Default to red as precaution if an error occurs
                statusColor = Color.parseColor("#fc0505");
                status = "RED";
                break;
        }

        ((TextView)findViewById(R.id.statusValueTxt)).setText(status);
        ((TextView)findViewById(R.id.statusValueTxt)).setTextColor(statusColor);
        findViewById(R.id.statusColourContainer).setBackgroundColor(statusColor);
    }
}