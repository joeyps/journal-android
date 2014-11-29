package com.thosedays.ui;

import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Created by joey on 14/11/29.
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
