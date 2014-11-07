package com.thosedays.com.thosedays.utils;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by joey on 14/11/6.
 */
public class Worker {

    private static final Object sLock = new Object();
    private static Handler mWorker;
    private static HandlerThread mThread;

    public static Handler get() {
        synchronized (sLock) {
            if (mThread == null) {
                mThread = new HandlerThread(Worker.class.getSimpleName());
                mThread.start();
                mWorker = new Handler(mThread.getLooper());
            }
        }
        return mWorker;
    }
}
