package com.mdiakonov.c3view;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

public class MyGroupCursorLoader extends CursorLoader {
    WorkersDbAdapter db;
    public MyGroupCursorLoader(Context context, WorkersDbAdapter db) {
        super(context);
        this.db = db;
    }

    @Override
    public Cursor loadInBackground() {
        return db.getSpecialtyData();
    }
}