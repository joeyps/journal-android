package com.thosedays.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thosedays.util.LogUtils.*;

/**
 * Created by joey on 14/11/8.
 */
public class SelectionBuilder {
    private static final String TAG = makeLogTag(SelectionBuilder.class);

    private String mTable = null;
    private StringBuilder mSelection = new StringBuilder();
    private Map<String, String> mProjectionMap = new HashMap<String, String>();
    private ArrayList<String> mSelectionArgs = new ArrayList<String>();
    private String mGroupBy = null;
    private String mHaving = null;

    public SelectionBuilder table(String table) {
        mTable = table;
        return this;
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            Collections.addAll(mSelectionArgs, selectionArgs);
        }

        return this;
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        return query(db, false, columns, orderBy, null);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, boolean distinct, String[] columns, String orderBy,
                        String limit) {
        assertTable();
        if (columns != null) mapColumns(columns);
        LOGV(TAG, "query(columns=" + Arrays.toString(columns)
                + ", distinct=" + distinct + ") " + this);
        return db.query(distinct, mTable, columns, getSelection(), getSelectionArgs(), mGroupBy,
                mHaving, orderBy, limit);
    }

    /**
     * Return selection string for current internal state.
     *
     * @see #getSelectionArgs()
     */
    public String getSelection() {
        return mSelection.toString();
    }

    /**
     * Return selection arguments for current internal state.
     *
     * @see #getSelection()
     */
    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    private void mapColumns(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Table not specified");
        }
    }
}
