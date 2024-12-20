package com.example.usimaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import com.example.usimaps.map.Graph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.Triple;

/**
 * Database Helper
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database
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


    /**
     * Constructor
     * @param context Context
     */
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

    /**
     * Updates the graph in the database: if the graph already exists, update it, otherwise insert it
     * @param graph Graph to update
     */
    public void updateGraph(Graph graph) {
        // save the graph in the database, if already exists, update
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String mapName = graph.getMapName();
        byte[] bytegraph = Graph.serialize(graph);
        values.put(DatabaseHelper.COLUMN_MAP_NAME, mapName);
        values.put(DatabaseHelper.COLUMN_MAP_OBJECT, bytegraph);

        // Check if the map already exists
        String selection = DatabaseHelper.COLUMN_MAP_NAME + " = ?";
        String[] selectionArgs = { mapName };

        int count = db.update(DatabaseHelper.TABLE_MAPS, values, selection, selectionArgs);

        // If the map does not exist, insert it
        if (count == 0) {
            db.insert(DatabaseHelper.TABLE_MAPS, null, values);
        }

        db.close();
    }

    /**
     * Load the graph from the database
     * @param name Name of the graph
     * @return Graph
     */
    public Graph loadGraph(String name) {
        // load the graph from the database
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_MAP_NAME,
                DatabaseHelper.COLUMN_MAP_OBJECT
        };
        String selection = DatabaseHelper.COLUMN_MAP_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MAPS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        Graph graph = null;
        if (cursor.moveToNext()) {
            byte[] bytegraph = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAP_OBJECT));
            graph = Graph.deserialize(bytegraph);
        }
        cursor.close();
        db.close();
        return graph;
    }

    public List<String> getMapNames() {
        // get the names of the maps in the database
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_MAP_NAME
        };
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MAPS,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        List<String> mapNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            String mapName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAP_NAME));
            mapNames.add(mapName);
        }
        cursor.close();
        db.close();
        return mapNames;
    }

    /**
     * Save the route start, end locations and date in the history
     * @param start Start location
     * @param end End location
     */
    public void saveHistory(String start, String end) {
        // save the start and end locations in the history database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // date
        Date date = new Date();
        // To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale
        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        String strDate = formatter.format(date);
        values.put(DatabaseHelper.COLUMN_DATE, strDate);
        values.put(DatabaseHelper.COLUMN_START, start);
        values.put(DatabaseHelper.COLUMN_GOAL, end);
        long newRowId = db.insert(DatabaseHelper.TABLE_HISTORY, null, values);
        db.close();
    }


    /**
     * Get the history of the start and end locations
     * @return List of triples with the date, start and end locations
     */
    public List<Triple<String, String, String>> getHistory() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_DATE,
                DatabaseHelper.COLUMN_START,
                DatabaseHelper.COLUMN_GOAL
        };
        String sortOrder = DatabaseHelper.COLUMN_ID + " DESC";
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_HISTORY,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        List<Triple<String, String, String>> history = new ArrayList<>();
        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
            String start = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START));
            String goal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL));
            history.add(new Triple<>(date, start, goal));

        }
        cursor.close();
        db.close();
        return history;
    }
}
