package com.mdiakonov.c3view;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mdiakonov.c3view.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationActivity extends ActionBarActivity {
    private ScheduledExecutorService scheduleTaskExecutor;
    private TextView mCoordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mCoordTextView = (TextView) findViewById(R.id.coord_text);

        scheduleTaskExecutor= Executors.newScheduledThreadPool(2);

        // This schedule a task to run every 10 minutes:
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // Parsing RSS feed:
                //myFeedParser.doSomething();

                // If you need update UI, simply do this:
                runOnUiThread(new Runnable() {
                    public void run() {
                        // update your UI component here.
                        mCoordTextView.setText("refreshed");
                    }
                });
            }
        }, 0, 1, TimeUnit.MINUTES);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
