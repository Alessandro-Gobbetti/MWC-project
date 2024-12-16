package com.example.usimaps;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usi_maps.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    public static final String TABLE_IMAGES = "images";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Maps table
    public static final String TABLE_MAPS = "maps";
    public static final String COLUMN_MAP_NAME = "map_name";
    public static final String COLUMN_MAP_OBJECT = "map_object";

    // History table
    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_GOAL = "goal";

    // Create table SQL
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_IMAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_IMAGE_PATH + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_DIRECTION + " REAL, " +
                    COLUMN_TIMESTAMP + " INTEGER" +
                    ");";

    private static final String CREATE_MAPS_TABLE =
            "CREATE TABLE " + TABLE_MAPS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MAP_NAME + " TEXT UNIQUE, " +
                    COLUMN_MAP_OBJECT + " TEXT" +
                    ");";

    private static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_START + " TEXT, " +
                    COLUMN_GOAL + " TEXT" +
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(CREATE_MAPS_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    //Figure this out exactly
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }
}
