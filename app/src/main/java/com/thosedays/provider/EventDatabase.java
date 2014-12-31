package com.thosedays.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.thosedays.provider.EventContract.*;

/**
 * Created by joey on 14/11/7.
 */
public class EventDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "events.db";
    private static final int CUR_DATABASE_VERSION = 1;

    private Context mContext;
    interface Tables {
        String EVENTS = "events";
        String TAGS = "tags";
    }


    public EventDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.EVENTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EventColumns.EVENT_ID + " TEXT NOT NULL,"
                + EventColumns.EVENT_DESCRIPTION + " TEXT NOT NULL,"
                + EventColumns.PHOTO_URL + " TEXT NOT NULL,"
                + EventColumns.PHOTO_WIDTH + " INTEGER NOT NULL,"
                + EventColumns.PHOTO_HEIGHT + " INTEGER NOT NULL,"
                + EventColumns.LOC_LAT + " REAL NOT NULL DEFAULT " + EventContract.INVALID_LOCATION + ","
                + EventColumns.LOC_LNG + " REAL NOT NULL DEFAULT " + EventContract.INVALID_LOCATION + ","
                + EventColumns.EVENT_TIME + " DATETIME NOT NULL,"
                + EventColumns.DELETED + " INTEGER DEFAULT 0,"
                + EventColumns.SYNCED + " INTEGER NOT NULL,"
                + "UNIQUE (" + EventColumns.EVENT_ID + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
