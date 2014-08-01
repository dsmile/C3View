package com.mdiakonov.c3view;

import android.content.Context;
import android.database.Cursor;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;

import java.util.List;
import android.view.Menu;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    final static int SPECIALTY_LOADER_ID = 2;
    // Callback'и для SPECIALTY_LOADER_ID
    private LoaderManager.LoaderCallbacks<Cursor> mSpecialtyListCallbacks;
    // Адаптер для данных табов
    SectionsPagerAdapter mSectionsPagerAdapter;

    ListAdapter sectionsListAdapter;
    SimpleCursorAdapter mDrawerAdapter;

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    WorkersDbAdapter dbHelper;

    // if run on phone, isSinglePane = true,  if run on tablet, isSinglePane = false
    static boolean isSinglePane;
    private static final String TAG = "MainActivity";
    PagerWithListFragment myListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.w("HEH", "Phone");

        View v = findViewById(R.id.phone_container);
        if(v == null){
            //it's run on tablet
            isSinglePane = false;
            // все фрагменты загружается в XML, нет необходимости делать это программно
        } else {
            // it's run on phone
            // Load MyListFragment programmatically
            isSinglePane = true;
            Log.w("HEH", "Phone");
            if (savedInstanceState == null) {
                //if's the first time created
                myListFragment = new PagerWithListFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.phone_container, myListFragment);
                fragmentTransaction.commit();
            }
        }

        // БД
        dbHelper = new WorkersDbAdapter(this);
        dbHelper.open();

        // если база данных пуста, загружаем данные после запуска
        if (dbHelper.getSpecialtyData().getCount() == 0) {
            UpdateDatabase();
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] listDataColumns = new String[] {WorkersDbAdapter.SPECIALTY_NAME};
        int[] listReflection = new int[] {android.R.id.text1};
        mDrawerAdapter = new SimpleCursorAdapter(this,
                R.layout.drawer_list_item, null,
                listDataColumns, listReflection, 0);
        // Set the adapter for the list view
        mDrawerList.setAdapter(mDrawerAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mSpecialtyListCallbacks = this;
        LoaderManager lm = getSupportLoaderManager();
        lm.initLoader(SPECIALTY_LOADER_ID, null, mSpecialtyListCallbacks);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        if (myListFragment != null) {
            myListFragment.SetCurrentTab(position);
        } else {
            // TODO
        }
        //mViewPager.setCurrentItem(position);
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
        } else if (id == R.id.action_delete) {
            // TODO при очистке таблиц не обновляются вкладки
            dbHelper.ClearTables();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int pageCount;
        private Cursor cursor = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (cursor != null) {
                Fragment fragment = new WorkersListFragment();
                Bundle args = new Bundle();
                cursor.moveToPosition(position);
                args.putInt("SPEC_ID", cursor.getInt(1));
                fragment.setArguments(args);
                return fragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
            // return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (cursor != null) {
                //            return "Менеджеры";
                cursor.moveToPosition(position);
                return cursor.getString(0);
            } else {
                return "Err";
            }
        }

        public void SetPageInfo(Cursor data) {
            cursor = data;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new MyGroupCursorLoader(this, dbHelper);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor result) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case SPECIALTY_LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                //mListAdapter.swapCursor(cursor);
 /*               mSectionsPagerAdapter.SetPageInfo(result);
                mSectionsPagerAdapter.notifyDataSetChanged();
*/
                mDrawerAdapter.swapCursor(result);
                break;
        }
        // The listview now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        // mListAdapter.swapCursor(null);
      /*  mSectionsPagerAdapter.SetPageInfo(null);
        mSectionsPagerAdapter.notifyDataSetChanged();
*/
        mDrawerAdapter.swapCursor(null);
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