package com.example.upgradedapp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("reminder_message");
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
