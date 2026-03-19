package com.example.upgradedapp2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recycler;
    NoteAdapter adapter;
    FloatingActionButton fab;
    ImageButton btnLogout;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        db = AppDatabase.getInstance(this);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        fab = findViewById(R.id.fabAdd);
        btnLogout = findViewById(R.id.btnLogout);

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditNoteActivity.class)));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadNotes();
    }

    private void loadNotes(){
        List<NoteEntity> notes = db.noteDao().getAll();
        adapter = new NoteAdapter(notes, this, db);
        recycler.setAdapter(adapter);
    }
}
