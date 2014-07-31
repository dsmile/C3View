package com.mdiakonov.c3view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MenuInflater;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public /*static*/ class WorkersListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
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

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("Не найдено");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        dbHelper = new WorkersDbAdapter(context);
        dbHelper.open();

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(context,
                R.layout.list_child, null,
                new String[] { WorkersDbAdapter.F_NAME, WorkersDbAdapter.L_NAME, WorkersDbAdapter.BIRTHDAY,
                               WorkersDbAdapter.AVATR_URL},
                null, 0) {
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

        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0,  getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.

        MenuItem item = menu.add("Поиск")
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
        Cursor cursor = mAdapter.getCursor();
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

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        MyChildCursorLoader loader;
        loader = new MyChildCursorLoader(context, dbHelper, (args.getInt("POS") + 101), args.getString("FILTER"));
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
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
