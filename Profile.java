package com.example.accidentdetection;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Profile extends AppCompatActivity {
    EditText etName, etAddress, etFatherMobile, etEmail;
    Button btnSave;
    ListView listView;
    DBHelper dbHelper;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ArrayList<Integer> idList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);


        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etFatherMobile = findViewById(R.id.etFatherMobile);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        listView = findViewById(R.id.listView);


        dbHelper = new DBHelper(this);
        list = new ArrayList<>();
        idList = new ArrayList<>();

        loadData();

        btnSave.setOnClickListener(v -> {
            dbHelper.insert(
                    etName.getText().toString(),
                    etAddress.getText().toString(),
                    etFatherMobile.getText().toString(),
                    etEmail.getText().toString()
            );
            clearFields();
            loadData();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            dbHelper.delete(idList.get(position));
            loadData();
        });

    }

    private void loadData() {
        list.clear();
        idList.clear();
        Cursor c = dbHelper.getAll();

        while (c.moveToNext()) {
            idList.add(c.getInt(0));
            list.add(
                    "Name: " + c.getString(1) +
                            "\nAddress: " + c.getString(2) +
                            "\nMobile: " + c.getString(3) +
                            "\nEmail: " + c.getString(4) +
                            "\n(Tap to Delete)"
            );
        }
        c.close();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    private void clearFields() {
        etName.setText("");
        etAddress.setText("");
        etFatherMobile.setText("");
        etEmail.setText("");
    }
}