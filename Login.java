package com.example.accidentdetection;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    EditText etLoginUserId, etLoginPassword;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        etLoginUserId = findViewById(R.id.etLoginUserId);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String userId = etLoginUserId.getText().toString();
            String password = etLoginPassword.getText().toString();

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter User ID and Password", Toast.LENGTH_SHORT).show();
            } else if (userId.equals("admin") && password.equals("admin")) {
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();

                // Navigate to Admin Dashboard
                Intent intent = new Intent(this, Profile.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
            }
        });

    }
}