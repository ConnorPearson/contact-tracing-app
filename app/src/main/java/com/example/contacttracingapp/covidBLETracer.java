package com.example.contacttracingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class covidBLETracer extends Service {
    private static final String TAG = "bluetooth";

    List<ScanFilter> filters = new ArrayList<>();

    AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .setConnectable(false)
            .build();

    private BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    private BluetoothLeScanner mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private JSONArray closeProximityUUIDs = new JSONArray();
    private UUID userUUID;
    private String userStatus = "";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        //Construct the service notification required for background services in newer android api's
        String NOTIFICATION_CHANNEL_ID = "com.example.contacttracingapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Corona virus contact tracing service")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        //Starts notification, needs to start within 5 seconds of serviceReceiver trigger
        startForeground(2, notification);

        //Load in string from file and convert to JSON for parsing
        try {
            JSONObject userDataJson = new JSONObject(fileReadWrite.loadFromFile(this, "userData.json"));
            userUUID = UUID.fromString(userDataJson.getString("uuid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + userUUID.toString());

        startLeScan();
        startLeAdvert();
        checkStatus();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    public void startLeScan() {
        //Setup scan settings, mode set to Low Power for battery decreased use
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private void checkStatus() {
        JSONObject userUUIDJson = new JSONObject();
        String prevUserStatus = userStatus;

        try {
            userUUIDJson.put("uuid", userUUID.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        postData("http://192.168.0.90:3000/getStatus", userUUIDJson.toString());

        //If status has changed and status is RED, post UUIDs of potentially exposed users
        if (!prevUserStatus.equals(userStatus)) {
            if(userStatus.equals("RED")) {
                postProximityUuids();
            }
        }
    }

    //Send uuids of devices that have been close to the symptomatic user
    private void postProximityUuids() {
        JSONArray exposedUuids = new JSONArray();

        try {
            exposedUuids = new JSONArray(fileReadWrite.loadFromFile(this, "exposedUuids.json"));
        } catch (Exception e) {
            Log.e(TAG, "postProximityUuids: ", e);
        }

        exposedUuids.put(userUUID);

        postData("http://192.168.0.90:3000/receiveProximityUuids", exposedUuids.toString());
    }

    public void postData(final String urlData, final String data)  {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlData);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());

                    os.writeBytes(data);

                    os.flush();
                    os.close();

                    if (connection.getResponseCode() == 200) {
                        InputStreamReader input = new InputStreamReader(connection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(input);

                        userStatus = bufferedReader.readLine();
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection error occurred! Please make sure you have an active internet connection, then try again.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(thread, 0, 10, TimeUnit.SECONDS);
    }

    public void startLeAdvert() {
        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

        //Add user specific uuid to parcel for broadcasting
        ParcelUuid uuidParcel = new ParcelUuid(userUUID);

        //Setup advertisement data preferences. Takes in user specific UUID
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(uuidParcel)
                .build();


        advertiser.startAdvertising(settings, data, advertisingCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        //Truncates data to derive UUID, no built in function for get UUID so this was the solution
        private UUID trimUUID(String data) {
            try {
                return UUID.fromString(data.substring(data.indexOf("mServiceUuids=[") + 15, data.indexOf("mServiceUuids=[") + 51));
            } catch (Exception e) {
                Log.i(TAG, "trimUUID: returned UUID value is invalid");
                return null;
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //If the scanned devices signal (representative of distance) is < value or null break
            if (result == null || result.getRssi() < -70)
                return;

            //Process trim on result UUID, getUUID is not a function of result by default
            UUID uuid = trimUUID(result.toString());

            //Read in proximity UUIDs file
            try {
                closeProximityUUIDs = new JSONArray(fileReadWrite.loadFromFile(getApplicationContext(), "proximityUuids.json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //If the UUID is already in ArrayList, don't write value
            assert uuid != null;
            if (!closeProximityUUIDs.toString().contains(uuid.toString())) {
                closeProximityUUIDs.put(uuid);

                //Write proximity uuids back to file
                fileReadWrite.writeToFile(closeProximityUUIDs.toString(), "proximityUuids.json", getApplicationContext());

                Log.i(TAG, "Adding UUID to file : " +  uuid);
            }

            Log.i(TAG, "onScanResult: " + closeProximityUUIDs.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Discovery onScanFailed: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };
}

