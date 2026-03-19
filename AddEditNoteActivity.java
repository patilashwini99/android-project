package com.example.upgradedapp2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class AddEditNoteActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 400;
    private static final int REQ_IMAGE_CAPTURE = 401;

    EditText etTitle, etContent;
    Button btnSave, btnCapture, btnSetReminder;

    AppDatabase db;
    long editingId = -1;
    long reminderMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = AppDatabase.getInstance(this);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        btnCapture = findViewById(R.id.btnCapture);
        btnSetReminder = findViewById(R.id.btnSetReminder);

        // Load existing note if editing
        if (getIntent().hasExtra("noteId")) {
            editingId = getIntent().getLongExtra("noteId", -1);
            NoteEntity n = db.noteDao().getById(editingId);
            if (n != null) {
                etTitle.setText(n.title);
                etContent.setText(n.content);
                reminderMillis = n.reminderMillis;
            }
        }

        // Capture button click
        btnCapture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            } else {
                dispatchTakePictureIntent();
            }
        });

        // Save button click
        btnSave.setOnClickListener(v -> saveNote());

        // Reminder button click
        btnSetReminder.setOnClickListener(v -> setReminderDialog());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Add title or content", Toast.LENGTH_SHORT).show();
            return;
        }

        String keywords = Utils.extractKeywords(content);

        if (editingId == -1) {
            NoteEntity note = new NoteEntity(title, content, keywords, reminderMillis);
            long id = db.noteDao().insert(note);
            if (reminderMillis > System.currentTimeMillis()) {
                setAlarm(id, title);
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } else {
            NoteEntity note = new NoteEntity(title, content, keywords, reminderMillis);
            note.id = editingId;
            db.noteDao().update(note);
            if (reminderMillis > System.currentTimeMillis()) {
                setAlarm(editingId, title);
            }
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void setReminderDialog() {
        final EditText et = new EditText(this);
        et.setHint("Reminder in minutes");

        new AlertDialog.Builder(this)
                .setTitle("Set reminder")
                .setView(et)
                .setPositiveButton("Set", (d, w) -> {
                    try {
                        int mins = Integer.parseInt(et.getText().toString().trim());
                        reminderMillis = System.currentTimeMillis() + mins * 60L * 1000L;
                        Toast.makeText(this, "Reminder set in " + mins + " minutes", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQ_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_CAMERA && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_IMAGE_CAPTURE && res == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                runTextRecognition(imageBitmap);
            }
        }
    }

    private void runTextRecognition(Bitmap bmp) {
        try {
            InputImage image = InputImage.fromBitmap(bmp, 0);
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(this::onTextRecognized)
                    .addOnFailureListener(e -> Toast.makeText(this, "OCR failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(this, "OCR init error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onTextRecognized(Text visionText) {
        String resultText = visionText.getText();
        if (resultText.isEmpty()) {
            Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
            return;
        }

        String current = etContent.getText().toString();
        if (!current.isEmpty()) current += "\n";
        etContent.setText(current + resultText);

        String kw = Utils.extractKeywords(etContent.getText().toString());
        Toast.makeText(this, "Keywords: " + kw, Toast.LENGTH_LONG).show();
    }

    private void setAlarm(long noteId, String title) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("noteId", noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, reminderMillis, pendingIntent);
        }
    }
}
