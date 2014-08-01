package com.mdiakonov.c3view;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.graphics.Bitmap;
import android.util.Log;

public /*static*/ class WorkersListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    // Идентификатор Loader для загрузки элементов списка
    private static final int LIST_LOADER_ID = 1;
    // Callback'и для LIST_LOADER_ID
    private LoaderManager.LoaderCallbacks<Cursor> mListCallbacks;
    // Адаптер для данных списка
    SimpleCursorAdapter mListAdapter;
    // Если не пустая строка (или null) - фильтр по именам работников в списке
    String mCurFilter;

    Context context;
    WorkersDbAdapter dbHelper;

    // Better than getActivity() as it returns null if onAttach isn't called yet.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Текст, когда список пуст
        setEmptyText(getResources().getText(R.string.no_data));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        dbHelper = new WorkersDbAdapter(context);
        dbHelper.open();

        // список названий необходимых столбцов из БД
        String[] listDataColumns = new String[] { WorkersDbAdapter.F_NAME, WorkersDbAdapter.L_NAME,
                WorkersDbAdapter.BIRTHDAY, WorkersDbAdapter.AVATR_URL};

        // Initialize the adapter. Note that we pass a 'null' Cursor as the
        // third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the
        // LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This
        // prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mListAdapter = new SimpleCursorAdapter(context,
                R.layout.list_child, null,
                listDataColumns, null, 0) {
                    @Override
                    public void bindView (View view, Context context, Cursor cursor) {
                        TextView txtListAge = (TextView) view.findViewById(R.id.lblListAge);
                        TextView txtListName = (TextView) view.findViewById(R.id.lblListChildName);
                        ImageView imgAvatar = (ImageView) view.findViewById(R.id.avatar);
                        // Имя + Фамилия
                        String name = DataConditioning.properCase(cursor.getString(2)) + " " +
                                DataConditioning.properCase(cursor.getString(1));

                        txtListName.setText(name);
                        txtListAge.setText(
                                DataConditioning.properAgeLabel(DataConditioning.properAge(cursor.getString(3))));
                        UrlImageViewHelper.setUrlDrawable(imgAvatar, cursor.getString(4), R.drawable.no_avatar,
                                new UrlImageViewCallback() {
                                    @Override
                                    public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url,
                                                         boolean loadedFromCache) {
                                        if (!loadedFromCache) {
                                            ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1,
                                                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                                            scale.setDuration(300);
                                            scale.setInterpolator(new OvershootInterpolator());
                                            imageView.startAnimation(scale);
                                        }
                                    }
                                }
                        );
                    }
                };
        // Associate the (now empty) adapter with the ListView.
        setListAdapter(mListAdapter);

        // The Activity (which implements the LoaderCallbacks<Cursor>
        // interface) is the callbacks object through which we will interact
        // with the LoaderManager. The LoaderManager uses this object to
        // instantiate the Loader and to notify the client when data is made
        // available/unavailable.
        mListCallbacks = this;

        // Initialize the Loader with id 'LIST_LOADER_ID' and callbacks 'mListCallbacks'.
        // If the loader doesn't already exist, one is created. Otherwise,
        // the already created Loader is reused. In either case, the
        // LoaderManager will manage the Loader across the Activity/Fragment
        // lifecycle, will receive any new loads once they have completed,
        // and will report this new data back to the 'mCallbacks' object.
        LoaderManager lm = getLoaderManager();
        lm.initLoader(LIST_LOADER_ID,  getArguments(), mListCallbacks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.

        MenuItem item = menu.add(R.string.search)
                .setIcon(android.R.drawable.ic_menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        View sv = SearchViewCompat.newSearchView(context);
        SearchViewCompat.setOnQueryTextListener(sv, mOnQueryTextListener);
        MenuItemCompat.setActionView(item, sv);
    }
    // The following callbacks are called for the SearchView.OnQueryChangeListener
// For more about using SearchView, see src/.../view/SearchView1.java and SearchView2.java

    private final SearchViewCompat.OnQueryTextListenerCompat mOnQueryTextListener =
            new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    // Called when the action bar search text has changed.  Update
                    // the search filter, and restart the loader to do a new query
                    // with this filter.
                    mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                    Bundle args = getArguments();
                    args.putString("FILTER", mCurFilter);
                    getLoaderManager().restartLoader(0, getArguments(), WorkersListFragment.this);
                    Log.w("HEH", "Filter: " + mCurFilter);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Don't care about this.
                    return true;
                }
            };

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("FragmentComplexList", "Item clicked: " + id);
        //Cursor cursor = mAdapter.getItem(position);
        Cursor cursor = mListAdapter.getCursor();
        int workerId = cursor.getInt(0);

        Cursor specs = dbHelper.getWorkerSpecialities(workerId);
        Log.w("DDATA", "Spec_cnt = " + specs.getCount());
        String specString = "";
        while (specs.moveToNext()) {
            specString += specs.getString(0) + " ";
        }
        Bundle args = new Bundle();
        args.putString("name", DataConditioning.properCase(cursor.getString(2)) + " " +
                DataConditioning.properCase(cursor.getString(1)));
        args.putString("birthday", cursor.getString(3));
        args.putString("avatar_url", cursor.getString(4));
        args.putString("specs", specString);

        Intent newActivity = new Intent(context, DetailsActivity.class);
        newActivity.putExtras(args);
        startActivity(newActivity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new MyChildCursorLoader(context, dbHelper,
                args.getInt("SPEC_ID"), args.getString("FILTER"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LIST_LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mListAdapter.swapCursor(cursor);
                break;
        }
        // The listview now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mListAdapter.swapCursor(null);
    }


    static class MyChildCursorLoader extends CursorLoader {
        WorkersDbAdapter db;
        int specId;
        String nameFilter;

        public MyChildCursorLoader(Context context, WorkersDbAdapter db, int specId, String nameFilter) {
            super(context);
            this.db = db;
            this.specId = specId;
            this.nameFilter = nameFilter;
        }

        @Override
        public Cursor loadInBackground() {
            if (TextUtils.isEmpty(nameFilter)) {
                return db.getWorkersData(specId);
            } else {
                return db.getWorkersDataByName(specId, nameFilter);
            }
        }
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // Do your stuff here
            if (!isVisibleToUser) {
                //Log.d("MyFragment", "Not visible");

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
