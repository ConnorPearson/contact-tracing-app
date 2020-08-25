package com.example.contacttracingapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * <h1>serviceReceiver.java.java</h1>
 * This class starts the covidBLETracer service if the device matches the compatible Android
 * version.
 * <p>
 * This class is most commonly called on boot of the user device when the service starts.
 *
 * @author  Connor Pearson
 * @since 2020-05-18
 */

public class serviceReceiver extends BroadcastReceiver{

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent arg1) {
        Intent intent = new Intent(context, covidBLETracer.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
