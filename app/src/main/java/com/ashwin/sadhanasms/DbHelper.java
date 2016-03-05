package com.ashwin.sadhanasms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ashwin on 11/23/2015.
 */
public class DbHelper extends SQLiteOpenHelper {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_COUNSELOR = "Counselor";
    public static final String COLUMN_CONTACT_NO = "Contact";
    public static final String COLUMN_TIME = "Time";

    //Database details
    private static final String DATABASE_NAME = "SadhanaSMS.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CREDENTIALS = "Credentials";
    private static final String CREATE_TABLE_CREDENTIALS = "CREATE TABLE " + TABLE_CREDENTIALS + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_COUNSELOR + " TEXT, " + COLUMN_CONTACT_NO + " TEXT, " + COLUMN_TIME + " TEXT)";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CREDENTIALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_CREDENTIALS);
        onCreate(db);
    }

    public long insert(String name, String counselor_name, String counselor_no, String sleepTime) {
        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        if (!name.equals("") && !counselor_name.equals("") && !counselor_no.equals("") && !sleepTime.equals(":")) {
            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_COUNSELOR, counselor_name);
            contentValues.put(COLUMN_CONTACT_NO, "+91" + counselor_no);
            contentValues.put(COLUMN_TIME, sleepTime);

            id = db.insert(TABLE_CREDENTIALS, null, contentValues);
        } else {
            throw new NullPointerException();
        }

        db.close();

        return id;
    }

    public void update(int id, String name, String counselor_name, String counselor_no, String sleepTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        if (!name.equals("") && !counselor_name.equals("") && !counselor_no.equals("") && !sleepTime.equals(":")) {

            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_COUNSELOR, counselor_name);
            contentValues.put(COLUMN_CONTACT_NO, "+91" + counselor_no);
            contentValues.put(COLUMN_TIME, sleepTime);

            db.update(TABLE_CREDENTIALS, contentValues, COLUMN_ID + " = " + id, null);
        } else
            throw new NullPointerException();


        db.close();
    }

    public boolean isNull() {
        SQLiteDatabase db = this.getWritableDatabase();

        String select = "SELECT * FROM " + TABLE_CREDENTIALS;

        Cursor cursor = db.query(TABLE_CREDENTIALS, null, null, null, null, null, null, "1");
        if (cursor.getCount() > 0) {
            return false;

        } else
            return true;
    }

    public Cursor getInfo() {
        SQLiteDatabase db = this.getWritableDatabase();

        String select = "SELECT * FROM " + TABLE_CREDENTIALS;

        Cursor cursor = db.query(TABLE_CREDENTIALS, null, null, null, null, null, null, "1");
        cursor.moveToFirst();

        return cursor;
    }
}
