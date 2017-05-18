package com.laxmi.smsparseinbackground;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private String TAG = "SMSTEST"+this.getClass().getSimpleName();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Debug.startMethodTracing("MainThread");
                Log.i(TAG, "Main thread : " + Thread.currentThread().getId());

                Callable callable = new SmsCallable(MainActivity.this);
                ThreadUtils.getDefaultExecutorService().submit(callable);
            }
        });
    }

    /*Runnable sendSMS= new Runnable() {
        @Override
        public void run() {

            PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0,
                    new Intent(MainActivity.this, MainActivity.class), 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("7503779778", null, "Test messages", pi, null);

            //if need sending status of sms then need to register broadcast receiver
            sendSMS("7503779778", "Test messages");

        }
    };*/


}
