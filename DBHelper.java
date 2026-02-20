package com.example.accidentdetection;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.List;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AccidentDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "EmergencyContact";
    public static final String COL_ID = "id";
    public static final String COL_SENDTO = "sendto";
    public static final String COL_MOBILE = "mobile";

    public static final String TABLE = "profile";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String MOBILE = "mobile";
    public static final String EMAIL = "email";

    // Singleton instance to avoid multiple helper instances/connections
    private static volatile DBHelper instance;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Obtain the singleton DBHelper. Prefer this in application code to avoid multiple DBHelper instances.
     * Uses the application context to avoid leaking an Activity context.
     */
    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SENDTO + " TEXT, " +
                COL_MOBILE + " TEXT)";
        db.execSQL(createTable);


        db.execSQL("CREATE TABLE " + TABLE + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NAME + " TEXT," +
                ADDRESS + " TEXT," +
                MOBILE + " TEXT," +
                EMAIL + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Use a transaction during upgrade to ensure consistency.
        db.beginTransaction();
        try {
            // Drop both tables if schema changes are incompatible. For real apps, implement
            // per-version migrations here instead of dropping data.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Insert emergency contact
    public boolean insertEmergencyContact(String sendto, String mobile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENDTO, sendto);
        values.put(COL_MOBILE, mobile);

        long result = -1;
        // perform write in a transaction
        db.beginTransaction();
        try {
            result = db.insert(TABLE_NAME, null, values);
            if (result != -1) db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result != -1;
    }

    /**
     * Returns a Cursor for all emergency contacts. Caller is responsible for closing the Cursor.
     */
    public Cursor getAllEmergencyContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_ID + " AS _id, " + COL_SENDTO + ", " + COL_MOBILE +
                        " FROM " + TABLE_NAME,
                null
        );
    }

    /**
     * Convenience method that returns all emergency contacts as a List of POJOs.
     * This is safer for callers who don't want to manage cursor lifecycle.
     */
    public List<EmergencyContact> getAllEmergencyContactsList() {
        List<EmergencyContact> list = new ArrayList<>();
        try (Cursor c = getAllEmergencyContacts()) {
            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow(COL_ID));
                    String sendto = c.getString(c.getColumnIndexOrThrow(COL_SENDTO));
                    String mobile = c.getString(c.getColumnIndexOrThrow(COL_MOBILE));
                    list.add(new EmergencyContact(id, sendto, mobile));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    // Delete contact by id
    public void deleteEmergencyContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }


    public void insert(String name, String address, String mobile, String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(ADDRESS, address);
        cv.put(MOBILE, mobile);
        cv.put(EMAIL, email);
        // wrap in transaction for safety
        db.beginTransaction();
        try {
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getAll() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE, null);
    }

    /**
     * Convenience method to return all profiles as POJOs and close cursor internally.
     */
    public List<Profile> getAllProfilesList() {
        List<Profile> list = new ArrayList<>();
        try (Cursor c = getAll()) {
            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow(ID));
                    String name = c.getString(c.getColumnIndexOrThrow(NAME));
                    String address = c.getString(c.getColumnIndexOrThrow(ADDRESS));
                    String mobile = c.getString(c.getColumnIndexOrThrow(MOBILE));
                    String email = c.getString(c.getColumnIndexOrThrow(EMAIL));
                    list.add(new Profile(id, name, address, mobile, email));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void delete(int id) {
        getWritableDatabase().delete(TABLE, ID + "=?", new String[]{String.valueOf(id)});
    }

    /**
     * Close the helper and clear the singleton instance.
     */
    @Override
    public synchronized void close() {
        super.close();
        instance = null;
    }

    // Small POJO classes to return typed data instead of forcing callers to work with Cursor.
    public static class EmergencyContact {
        public final int id;
        public final String sendTo;
        public final String mobile;

        public EmergencyContact(int id, String sendTo, String mobile) {
            this.id = id;
            this.sendTo = sendTo;
            this.mobile = mobile;
        }
    }

    public static class Profile {
        public final int id;
        public final String name;
        public final String address;
        public final String mobile;
        public final String email;

        public Profile(int id, String name, String address, String mobile, String email) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.mobile = mobile;
            this.email = email;
        }
    }

}
