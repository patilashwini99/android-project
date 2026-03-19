package com.example.upgradedapp2;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText etUser, etPass;
    Button btnRegister;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_register);
        prefs = getSharedPreferences("upgradedapp2_prefs", MODE_PRIVATE);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            if(u.isEmpty() || p.isEmpty()){
                Toast.makeText(this, "Fill fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(prefs.contains("user_"+u)){
                Toast.makeText(this, "User exists", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString("user_"+u, p);
            ed.apply();
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
