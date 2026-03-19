package com.example.upgradedapp2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {
    List<NoteEntity> notes;
    Context ctx;
    AppDatabase db;

    public NoteAdapter(List<NoteEntity> notes, Context ctx, AppDatabase db){
        this.notes = notes;
        this.ctx = ctx;
        this.db = db;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        final NoteEntity n = notes.get(pos);
        h.tvTitle.setText(n.title == null ? "(No title)" : n.title);
        h.tvContent.setText(n.content);
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, AddEditNoteActivity.class);
            i.putExtra("noteId", n.id);
            ctx.startActivity(i);
        });
        h.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ctx)
                    .setTitle("Delete?")
                    .setMessage("Delete this note?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.noteDao().delete(n);
                        notes.remove(pos);
                        notifyItemRemoved(pos);
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount(){ return notes.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
