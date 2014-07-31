package com.mdiakonov.c3view;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import static android.provider.BaseColumns._ID;

public class WorkersDbAdapter {
    private static final String DATABASE_NAME = "workers.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_WORKERS_NAME = "workers";
    // Столбцы в таблице "workers"
    public static final String F_NAME = "f_name";
    public static final String L_NAME = "l_name";
    public static final String BIRTHDAY = "birthday";
    public static final String AVATR_URL = "avatr_url";
    public static final String SPECIALTY_ID = "specialty_id";

    public static final String TABLE_SPECIALTY_NAME = "specialty";
    // Столбцы в таблице "specialty"
    public static final String SPECIALTY_NAME = "specialty_name";
    public static final String WORKER_ID = "worker_id";

    private static final String TAG = "WorkersDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_WORKERS_NAME + " (" + _ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + L_NAME
                    + " TEXT NOT NULL, " + F_NAME + " TEXT NOT NULL, "
                    + BIRTHDAY + " TEXT, "
                    + AVATR_URL + " TEXT, "
                    + SPECIALTY_ID + " INTEGER);");
            db.execSQL("CREATE TABLE " + TABLE_SPECIALTY_NAME + " (" + _ID
                    + " INTEGER PRIMARY KEY, " + SPECIALTY_NAME
                    + " TEXT NOT NULL, " + SPECIALTY_ID
                    + " INTEGER NOT NULL, " + WORKER_ID
                    + " INTEGER NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading db from " + oldVersion + " to "
                    + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKERS_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPECIALTY_NAME);
            onCreate(db);
        }
    }

    public WorkersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public WorkersDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    // данные по специальностям
    public Cursor getSpecialtyData() {
        return mDb.rawQuery("SELECT DISTINCT " + SPECIALTY_NAME + ", " + SPECIALTY_ID + " AS " +
                            _ID + " FROM " + TABLE_SPECIALTY_NAME, null);
    }

    // данные по рабочему конкретной специальности
    public Cursor getWorkersData(int specId) {
        Log.w(TAG, "SpecID = " + specId);
        return mDb.rawQuery("SELECT * FROM " + TABLE_WORKERS_NAME + " w INNER JOIN " +
                            TABLE_SPECIALTY_NAME + " s ON w." + _ID + " = s." +
                            WORKER_ID + " WHERE s." + SPECIALTY_ID + " = ?",
                            new String[]{String.valueOf(specId)});
    }

    // данные по рабочему конкретной специальности с именем или фамилией
    public Cursor getWorkersDataByName(int specId, String nameFilter) {
        return mDb.rawQuery("SELECT * FROM " + TABLE_WORKERS_NAME + " w INNER JOIN " +
                            TABLE_SPECIALTY_NAME + " s ON w." + _ID + " = s." +
                            WORKER_ID + " WHERE s." + SPECIALTY_ID + " = ?" +
                            " AND (w." + L_NAME + " LIKE '%"+nameFilter+"%' OR w." + F_NAME +
                            " LIKE '%"+nameFilter+"%')",
                            new String[]{String.valueOf(specId)});
    }

    // все специальности работника
    public Cursor getWorkerSpecialities(int workerId) {
        return mDb.rawQuery("SELECT " + SPECIALTY_NAME + " FROM " + TABLE_SPECIALTY_NAME +
                            " WHERE " + WORKER_ID + " = ?",
                            new String[]{String.valueOf(workerId)});
    }

    public long createSpeciality(int specialty_id, String specialty_name, long worker_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(SPECIALTY_ID, specialty_id);
        initialValues.put(SPECIALTY_NAME, specialty_name);
        initialValues.put(WORKER_ID, worker_id);
        return mDb.insert(TABLE_SPECIALTY_NAME, null, initialValues);
    }

    public long createWorker(String f_name, String l_name, String birthday,
                             String avatr_url, int specialty_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(F_NAME, f_name);
        initialValues.put(L_NAME, l_name);
        initialValues.put(BIRTHDAY, birthday);
        initialValues.put(AVATR_URL, avatr_url);
        initialValues.put(SPECIALTY_ID, specialty_id);
        return mDb.insert(TABLE_WORKERS_NAME, null, initialValues);
    }

    public Cursor fetchWorkerById(int id) throws SQLException {
        return mDb.query(true, TABLE_WORKERS_NAME, new String[] {F_NAME, L_NAME, BIRTHDAY,
                        AVATR_URL},
                _ID + " = " + id, null, null, null, null, null);
    }

    public void ClearTables() {
        mDb.execSQL("DELETE FROM " + TABLE_WORKERS_NAME);
        mDb.execSQL("DELETE FROM " + TABLE_SPECIALTY_NAME);
    }

}
