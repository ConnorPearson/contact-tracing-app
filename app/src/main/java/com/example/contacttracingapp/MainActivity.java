package com.example.contacttracingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    Bitmap bitmap;
    ImageView QRCodeImg;
    JSONObject userData;
    boolean isInitialSetup;

    private boolean loadUserData() {
        try {
            InputStream inputStream = getApplicationContext().openFileInput("userData.json");

            if (inputStream != null) {
                InputStreamReader inputReader = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(inputReader);

                userData = new JSONObject(br.readLine());

                inputStream.close();

                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkInitialSetup() {  //Check if app is starting for first time
        if (isInitialSetup) {
            Intent intent = new Intent(this, setup.class);
            String message = "Hello world";
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isInitialSetup = !loadUserData();
        checkInitialSetup();

        try {
            ((TextView)findViewById(R.id.nameTxtView)).setText(getString(R.string.nameTxtView,userData.get("firstname"), userData.get("surname")));
            ((TextView)findViewById(R.id.addressTxtView)).setText(getString(R.string.addressTxtView,userData.get("address")));
            ((TextView)findViewById(R.id.passportNumTxtView)).setText(getString(R.string.passportNumTxtView,userData.get("passportid")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            QRCodeImg = findViewById(R.id.QRCodeImg);

            bitmap = TextToImageEncode(userData.toString());
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
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 144, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
}