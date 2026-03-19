package com.example.upgradedapp2;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    private Button btnPickDate, btnPickTime, btnSetReminder;
    private TextView txtSelected;

    // Calendar for storing selected date/time
    private Calendar calendar = Calendar.getInstance();
    private int year, month, day, hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminder); // Make sure this layout exists

        // Initialize views
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        txtSelected = findViewById(R.id.txtSelected);

        // Set click listeners
        btnPickDate.setOnClickListener(v -> pickDate());
        btnPickTime.setOnClickListener(v -> pickTime());
        btnSetReminder.setOnClickListener(v -> setReminderAlarm());
    }

    // ---------------- PICK DATE --------------------
    private void pickDate() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, yr, mn, dy) -> {
                    year = yr;
                    month = mn;
                    day = dy;
                    updateText();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // ---------------- PICK TIME --------------------
    private void pickTime() {
        Calendar c = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hr, min) -> {
                    hour = hr;
                    minute = min;
                    updateText();
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                false
        );

        dialog.show();
    }

    // -------- UPDATE SELECTED DATE TIME TEXT ---------
    private void updateText() {
        txtSelected.setText(
                "Selected: " + day + "/" + (month + 1) + "/" + year +
                        "  " + hour + ":" + String.format("%02d", minute)
        );
    }

    // -------------- SET ALARM MANAGER ------------------
    private void setReminderAlarm() {

        if (year == 0) {
            Toast.makeText(this, "Please select date and time!", Toast.LENGTH_SHORT).show();
            return;
        }

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        if (triggerTime < System.currentTimeMillis()) {
            Toast.makeText(this, "Please choose a future time!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ReminderActivity.this, ReminderReceiver.class);
        intent.putExtra("reminder_message", "Your note reminder!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        Toast.makeText(this, "Reminder Set Successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
