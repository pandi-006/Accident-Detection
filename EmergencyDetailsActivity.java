package com.example.accidentdetection;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EmergencyDetailsActivity extends AppCompatActivity {

    EditText etSendTo, etMobile;
    Button btnSave;
    ListView listView;
    DBHelper dbHelper;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_details);

        etSendTo = findViewById(R.id.et_sendto);
        etMobile = findViewById(R.id.et_mobile);
        btnSave = findViewById(R.id.btn_save);
        listView = findViewById(R.id.list_emergency);

        dbHelper = new DBHelper(this);

        loadList();

        btnSave.setOnClickListener(view -> {
            String name = etSendTo.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.insertEmergencyContact(name, mobile);
            etSendTo.setText("");
            etMobile.setText("");
            loadList();
        });
    }

    private void loadList() {
        Cursor cursor = dbHelper.getAllEmergencyContacts();
        EmergencyAdapter adapter = new EmergencyAdapter(this, cursor, dbHelper);
        listView.setAdapter(adapter);
    }

}
