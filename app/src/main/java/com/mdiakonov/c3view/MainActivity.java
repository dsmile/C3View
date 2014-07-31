package com.mdiakonov.c3view;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks <Cursor> {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    WorkersDbAdapter dbHelper;
    // if run on phone, isSinglePane = true,  if run on tablet, isSinglePane = false
    static boolean isSinglePane;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // БД
        dbHelper = new WorkersDbAdapter(this);
        dbHelper.open();

        // если база данных пуста, загружаем данные после запуска
        if (dbHelper.getSpecialtyData().getCount() == 0) {
            UpdateDatabase();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_update) {
            UpdateDatabase();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new WorkersListFragment();
            Bundle args = new Bundle();
            args.putInt("POS", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return "Менеджеры";
        }
    }

    private class UpdateDatabase extends AsyncTask<String, Long, Long> {
        private String Content = null;
        private String Error = null;

        protected void onPreExecute() {
            //Start Progress Dialog (Message)
            Toast.makeText(MainActivity.this, R.string.update_db_start, Toast.LENGTH_SHORT).show();
        }

        protected Long doInBackground(String... urls) {
            BufferedReader reader=null;
            // Send data
            try {
                // Defined URL  where to send data
                URL url = new URL(urls[0]);
                Log.w(TAG, url.toString());

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                Log.w(TAG, "Conn opened");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                Log.w(TAG, "Conn opened");
                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                Log.w(TAG, "Start read");
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line);
                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
                // Append Server Response To Content String
                if (!isCancelled()) {
                    Content = sb.toString();
                }
            }
            catch(Exception ex) {
                Error = ex.getMessage();

            }
            finally {
                try {
                    reader.close();
                } catch(Exception ex) {
                    // TODO
                }
            }
            return 0l;
        }

        protected void onPostExecute(Long result) {
            if (Error != null) {
                Toast.makeText(MainActivity.this, R.string.update_db_error, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Output: "+Error);
            } else {
                // Файл с данными получен, можно очистить локальную БД
                dbHelper.ClearTables();
                // Разбор файла с данными
                JSONObject jsonResponse;
                try {
                    jsonResponse = new JSONObject(Content);
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("response");

                    int lengthJsonArr = jsonMainNode.length();
                    for (int i=0; i < lengthJsonArr; i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                        String f_name = jsonChildNode.optString("f_name").toLowerCase();
                        String l_name = jsonChildNode.optString("l_name").toLowerCase();
                        String birthday = DataConditioning.properDate(jsonChildNode.optString("birthday"));
                        String avatr_url = jsonChildNode.optString("avatr_url");

                        long worker_id = dbHelper.createWorker(f_name, l_name, birthday, avatr_url, 0);
                        // Если запись "Работник" добавлена в БД
                        if (worker_id != -1) {
                            // Разбор специальностей
                            JSONArray jsonSpecs = jsonChildNode.getJSONArray("specialty");

                            for (int j = 0; j < jsonSpecs.length(); j++) {
                                JSONObject jsonSpecItem = jsonSpecs.getJSONObject(j);
                                int specialty_id = jsonSpecItem.getInt("specialty_id");
                                // Если не будет названия специальности, вызвать исключение
                                String specName = jsonSpecItem.getString("name");
                                Log.w(TAG, "Spec: " + String.valueOf(specialty_id) + " - " + specName + ">" + worker_id);
                                dbHelper.createSpeciality(specialty_id, specName, worker_id);
                            }
                        }
                    }
                    Toast.makeText(MainActivity.this, R.string.update_db_end, Toast.LENGTH_SHORT).show();
                    UpdateListView();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onCancelled (Long result) {
            // TODO
        }
    }


    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyGroupCursorLoader(this, dbHelper);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }



    static class MyGroupCursorLoader extends CursorLoader {
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

    protected void UpdateListView() {
        Loader<Cursor> loader = getSupportLoaderManager().getLoader(-1);
        if (loader != null && !loader.isReset()) {
            getSupportLoaderManager().restartLoader(-1, null, this);
        } else {
            getSupportLoaderManager().initLoader(-1, null, this);
        }
    }
    protected void UpdateDatabase() {
        new UpdateDatabase().execute("http://65apps.com/images/testTask.json");
    }

    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}