package com.laxmi.smsparseinbackground;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by laxmi on 16/05/17.
 */

public class SmsCallable implements Callable {

    Context context;
    Handler myHandler;
    Runnable myRunnable;
    private String TAG = "SMSTEST" + this.getClass().getSimpleName();
    Handler handler;
    private Looper mLooper;

    public SmsCallable(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Object call() throws Exception {

        /*PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("7503779778", null, "Test messages", pi, null);*/

        //if need sending status of sms then need to register broadcast receiver

        //waitUntilReady();

        Log.i(TAG, "SmsCallable call() method initiated");
        Log.i(TAG, "Worker thread : " + Thread.currentThread().getId());
        sendSMS("+918114403447", "Test messages");

        return null;
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        HandlerThread handlerThread = new HandlerThread("ht");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
       /* Looper looper = Looper.myLooper();*/
        handler = new Handler(looper);
        context.registerReceiver(sentReceiver, new IntentFilter(SENT), null, handler); // Will not run on main thread

        //---when the SMS has been delivered---
        //context.registerReceiver(smsDelievredReceiver, new IntentFilter(DELIVERED), null, handler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            /*for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                int subscriptionId = subscriptionInfo.getSubscriptionId();
                Log.d("apipas", "subscriptionId:" + subscriptionId);
            }*/
            if (subscriptionInfoList.size() > 0) {
                SmsManager sms = SmsManager.getSmsManagerForSubscriptionId(subscriptionInfoList.get(0).getSubscriptionId());
                sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
            }
        } else {
            SimUtil.sendSMS(context, 0, phoneNumber, null, message, sentPI, null);
        }
    }

    private void receiveSms() {
        Log.i(TAG, "receiveSms() method has been called");
        context.registerReceiver(receiveReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"), null, handler);

        myHandler = new Handler();

        myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Worker thread : " + Thread.currentThread().getId());
                context.unregisterReceiver(receiveReceiver);
                context.unregisterReceiver(sentReceiver);
                Toast.makeText(context, "SMS NOT RECEIVED WITHIN 1 MINUTE",
                        Toast.LENGTH_SHORT).show();
            }
        };

        myHandler.postDelayed(myRunnable, 1000 * 60);
    }


    public synchronized void waitUntilReady() {
        //handler = new Handler(getLooper());
        getLooper();
    }

    public Looper getLooper() {
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            //notifyAll();
        }
        //Looper.loop();

        /*synchronized (this) {
            while (mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }*/
        return mLooper;
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "SMS sent receiver Worker thread : " + Thread.currentThread().getId());
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent",
                            Toast.LENGTH_SHORT).show();
                    receiveSms();

                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "Generic failure",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    BroadcastReceiver smsDelievredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Worker thread : " + Thread.currentThread().getId());
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered",
                            Toast.LENGTH_SHORT).show();

                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    BroadcastReceiver receiveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Sms received receiver Worker thread : " + Thread.currentThread().getId());
            Toast.makeText(context, "SMS RECEIVED",
                    Toast.LENGTH_SHORT).show();
            //Debug.stopMethodTracing();
            myHandler.removeCallbacks(myRunnable);

            try {
                context.unregisterReceiver(sentReceiver);
                context.unregisterReceiver(receiveReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }
    };
}
