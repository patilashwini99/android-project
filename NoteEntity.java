package com.example.upgradedapp2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String content;
    public String keywords;
    public long reminderMillis;

    public NoteEntity(String title, String content, String keywords, long reminderMillis){
        this.title = title;
        this.content = content;
        this.keywords = keywords;
        this.reminderMillis = reminderMillis;
    }
}
