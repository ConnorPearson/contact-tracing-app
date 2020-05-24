package com.example.contacttracingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            userData = new JSONObject(Objects.requireNonNull(data.getStringExtra("userdata")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setupPageAttributes();
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
        startActivity(intent);
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
}