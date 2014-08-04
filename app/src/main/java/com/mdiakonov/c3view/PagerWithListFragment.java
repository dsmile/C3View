package com.mdiakonov.c3view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class PagerWithListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    final static int SPECIALTY_LOADER_ID = 3;
    // Callback'и для SPECIALTY_LOADER_ID
    private LoaderManager.LoaderCallbacks<Cursor> mSpecialtyListCallbacks;
    // Адаптер для данных табов
    SectionsPagerAdapter mSectionsPagerAdapter;

    Context context;
    WorkersDbAdapter dbHelper;

    ViewPager mViewPager;

    public void OnDbChanges() {
        Log.w("UPDD", "OnDbChanges");
        Loader<Cursor> loader = getLoaderManager().getLoader(SPECIALTY_LOADER_ID);
        if (loader != null && !loader.isReset()) {
            Log.w("UPDD", "restartLoader");
            getLoaderManager().restartLoader(SPECIALTY_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(SPECIALTY_LOADER_ID, null, this);
        }
    }

    // Better than getActivity() as it returns null if onAttach isn't called yet.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        dbHelper = new WorkersDbAdapter(context);
        dbHelper.open();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) getView().findViewById(R.id.pager);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        } else {
            Log.w("wew", "NULL");
        }

        mSpecialtyListCallbacks = this;
        LoaderManager lm = getLoaderManager();
        lm.initLoader(SPECIALTY_LOADER_ID, null, mSpecialtyListCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_with_list, null);
        return view;
    }

    public void SetCurrentTab(int position) {
        mViewPager.setCurrentItem(position);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
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
                // TODO показать красиво, что записей в базе данных нет
                Log.w("UPDD", "getItem " + position);
                return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            Log.w("UPDD", "getItemPosition " + cursor.toString());
            if (cursor != null && (cursor.getCount() > 0)) {
                return POSITION_UNCHANGED;
            } else {
                return POSITION_NONE;
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
        return new MyGroupCursorLoader(context, dbHelper);
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
                Log.w("UPDD", "onLoadFinished " + result.toString());
                mSectionsPagerAdapter.SetPageInfo(result);
                mSectionsPagerAdapter.notifyDataSetChanged();

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
        Log.w("UPDD", "onLoaderFinished");
        mSectionsPagerAdapter.SetPageInfo(null);
        mSectionsPagerAdapter.notifyDataSetChanged();

    }
}