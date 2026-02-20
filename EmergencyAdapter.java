package com.example.accidentdetection;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cursoradapter.widget.CursorAdapter;

public class EmergencyAdapter extends CursorAdapter {

    DBHelper dbHelper;

    public EmergencyAdapter(Context context, Cursor c, DBHelper dbHelper) {
        super(context, c, 0);
        this.dbHelper = dbHelper;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.emergency_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvMobile = view.findViewById(R.id.tv_mobile);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SENDTO));
        String mobile = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MOBILE));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));

        tvName.setText(name);
        tvMobile.setText(mobile);

        btnDelete.setOnClickListener(v -> {
            dbHelper.deleteEmergencyContact(id);
            changeCursor(dbHelper.getAllEmergencyContacts());
            Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show();
        });
    }
}
