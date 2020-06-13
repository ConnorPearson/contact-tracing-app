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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private ArrayList<UUID> closeProximityUUIDs = new ArrayList<>();
    private UUID userUUID = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        //Load UUID from file
        try {
            userUUID = UUID.fromString(MainActivity.loadUserData(this).getString("uuid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        //Starts notification, needs to start whithin 5 seconds of serviceReceiver trigger
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + userUUID.toString());

        startLeScan();
        startLeAdvert();

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

            //If the UUID is already in ArrayList, don't write value
            if (!closeProximityUUIDs.contains(uuid)) {
                closeProximityUUIDs.add(uuid);
            }

            Log.i(TAG, String.valueOf(result));
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

