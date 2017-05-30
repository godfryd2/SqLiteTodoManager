package com.example.henas.aplikacja.database;

/**
 * Created by Henas on 07.11.2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.henas.aplikacja.model.TodoTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class TodoDbAdapter {
    private static final String DEBUG_TAG = "SqLiteTodoManager";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "database.db";
    private static final String DB_TODO_TABLE = "todo";

    public static final String KEY_ID = "_id";

    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final int ID_COLUMN = 0;

    public static final String KEY_DESCRIPTION = "description";
    public static final String DESCRIPTION_OPTIONS = "TEXT NOT NULL";
    public static final int DESCRIPTION_COLUMN = 1;

    public static final String KEY_DATE = "date";
    public static final String DATE_OPTIONS = "TEXT NOT NULL";
    public static final int DATE_COLUMN = 2;

    public static final String KEY_COMPLETED = "completed";
    public static final String COMPLETED_OPTIONS = "INTEGER DEFAULT 0";
    public static final int COMPLETED_COLUMN = 3;

    public static final String KEY_STATUS = "updateStatus";
    public static final String STATUS_OPTIONS = "TEXT NOT NULL";
    public static final int STATUS_COLUMN = 4;

    private static final String DB_CREATE_TODO_TABLE =
            "CREATE TABLE " + DB_TODO_TABLE + "( " +
                    KEY_ID + " " + ID_OPTIONS + ", " +
                    KEY_DESCRIPTION + " " + DESCRIPTION_OPTIONS + ", " +
                    KEY_DATE + " " + DATE_OPTIONS + ", " +
                    KEY_COMPLETED + " " + COMPLETED_OPTIONS + ", " +
                    KEY_STATUS + " " + STATUS_OPTIONS +
                    ");";
    private static final String DROP_TODO_TABLE =
            "DROP TABLE IF EXISTS " + DB_TODO_TABLE;

    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TODO_TABLE);

            Log.d(DEBUG_TAG, "Database creating...");
            Log.d(DEBUG_TAG, "Table " + DB_TODO_TABLE + " ver." + DB_VERSION + " created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TODO_TABLE);

            Log.d(DEBUG_TAG, "Database updating...");
            Log.d(DEBUG_TAG, "Table " + DB_TODO_TABLE + " updated from ver." + oldVersion + " to ver." + newVersion);
            Log.d(DEBUG_TAG, "All data is lost.");

            onCreate(db);
        }
    }

    public TodoDbAdapter(Context context) {
        this.context = context;
    }

    public TodoDbAdapter open(){
        dbHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (android.database.SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertTodo(String description, String date) {
        ContentValues newTodoValues = new ContentValues();
        newTodoValues.put(KEY_DESCRIPTION, description);
        newTodoValues.put(KEY_DATE, date);
        newTodoValues.put(KEY_STATUS, "no");
        return db.insert(DB_TODO_TABLE, null, newTodoValues);
    }

    public boolean updateTodo(TodoTask task) {
        long id = task.getId();
        String description = task.getDescription();
        String date = task.getDate();
        boolean completed = task.isCompleted();
        String status = task.getStatus();
        return updateTodo(id, description, date, completed, status);
    }

    public boolean updateTodo(long id, String description, String date, boolean completed, String status) {
        String where = KEY_ID + "=" + id;
        int completedTask = completed ? 1 : 0;
        ContentValues updateTodoValues = new ContentValues();
        updateTodoValues.put(KEY_DESCRIPTION, description);
        updateTodoValues.put(KEY_DATE, date);
        updateTodoValues.put(KEY_COMPLETED, completedTask);
        updateTodoValues.put(KEY_STATUS, status);
        return db.update(DB_TODO_TABLE, updateTodoValues, where, null) > 0;
    }

    public boolean deleteTodo(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(DB_TODO_TABLE, where, null) > 0;
    }

    public Cursor getAllTodos() {
        String[] columns = {KEY_ID, KEY_DESCRIPTION, KEY_DATE, KEY_COMPLETED, KEY_STATUS};
        return db.query(DB_TODO_TABLE, columns, null, null, null, null, null);
    }

    public TodoTask getTodo(long id) {
        String[] columns = {KEY_ID, KEY_DESCRIPTION, KEY_DATE, KEY_COMPLETED, KEY_STATUS};
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(DB_TODO_TABLE, columns, where, null, null, null, null);
        TodoTask task = null;
        if(cursor != null && cursor.moveToFirst()) {
            String description = cursor.getString(DESCRIPTION_COLUMN);
            String date = cursor.getString(DATE_COLUMN);
            boolean completed = cursor.getInt(COMPLETED_COLUMN) > 0 ? true : false;
            String status = cursor.getString(STATUS_COLUMN);
            task = new TodoTask(id, description, date, completed, status);
        }
        return task;
    }

    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM todo";
        open();
        SQLiteDatabase database = db;
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_id", cursor.getString(0));
                map.put("description", cursor.getString(1));
                map.put("date", cursor.getString(2));
                map.put("complete", cursor.getString(3));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM todo where updateStatus = '"+"no"+"'";
        open();
        SQLiteDatabase database = db;
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_id", cursor.getString(0));
                map.put("description", cursor.getString(1));
                map.put("date", cursor.getString(0));
                map.put("complete", cursor.getString(1));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync neededn";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM todo where updateStatus = '"+"no"+"'";
        open();
        SQLiteDatabase database = db;
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        open();
        SQLiteDatabase database = db;
        String updateQuery = "Update todo set updateStatus = '"+ status +"' where _id="+"'"+ id +"'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
    }
}