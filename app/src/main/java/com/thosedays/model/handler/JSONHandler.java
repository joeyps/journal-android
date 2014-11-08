package com.thosedays.model.handler;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by joey on 14/11/8.
 */
public abstract class JSONHandler {

    protected static Context mContext;

    public JSONHandler(Context context) {
        mContext = context;
    }

    public abstract void makeContentProviderOperations(ArrayList<ContentProviderOperation> list);

    public abstract void process(String json);
}