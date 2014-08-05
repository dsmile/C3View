package com.mdiakonov.c3view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;


import com.google.android.gms.location.LocationClient;
//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.mdiakonov.c3view.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.location.LocationListener;

public class LocationActivity extends ActionBarActivity  implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private ScheduledExecutorService scheduleTaskExecutor;
    private TextView mCoordTextView;
    private ProgressBar mActivityIndicator;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    LocationClient mLocationClient;
    Location mLocation;
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    boolean mUpdatesRequested;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

//    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mCoordTextView = (TextView) findViewById(R.id.coord_text);
        mActivityIndicator =
                (ProgressBar) findViewById(R.id.address_progress);

        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        // Start with updates turned off
        mUpdatesRequested = false;


        if (!initLocationProviders()) {
            showSettingsAlert();
        }

        if (initLocationProviders()) {
            startRequests();
        } else {
            mCoordTextView.setText("Сервисы местоположения недоступны, включите их и перезапустите приложение");
        }

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
/*        mLocationClient = new LocationClient(this, this, this);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
*/
    }

    public boolean initLocationProviders() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return (isGPSEnabled | isNetworkEnabled);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void startRequests() {
        try {
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled

            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     /**
      * Function to show settings alert dialog
      * On pressing Settings button will lauch Settings Options
      * */
     public void showSettingsAlert(){
         final Context mContext = this;

         AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

         // Setting Dialog Title
         alertDialog.setTitle("GPS settings");

         // Setting Dialog Message
         alertDialog.setMessage("GPS выключено, показать настройки?");

         // On pressing Settings button
         alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,int which) {
                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 mContext.startActivity(intent);
             }
         });

         // on pressing cancel button
         alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
             }
         });

         // Showing Alert Message
         alertDialog.show();
     }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Местоположение: " +
                Double.toString(location.getLatitude()) + ", " +
                Double.toString(location.getLongitude());
        mCoordTextView.setText(msg);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        if (mUpdatesRequested) {
           // mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
                //df
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            // TODO
            //showErrorDialog();
        }
    }

    /*
 * Called when the Activity becomes visible.
 */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
       // mLocationClient.connect();
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }

    @Override
    protected void onResume() {
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);
            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
        super.onResume();
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    protected void onStop() {
        // If the client is connected
       // if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
           //U mLocationClient.removeLocationUpdates(this);
        //}
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
       // mLocationClient.disconnect();
        super.onStop();
    }


    /**
     * The "Get Address" button in the UI is defined with
     * android:onClick="getAddress". The method is invoked whenever the
     * user clicks the button.
     *
     * @param v The view object associated with this method,
     * in this case a Button.
     */
    public void getAddress(View v) {
        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {
            if (location != null ) {
                // Turn the indefinite activity indicator on
                mActivityIndicator.setVisibility(View.VISIBLE);
                // Start the background task
                (new GetAddressTask(this)).execute(location);
            }

        }
    }

    /**
     * A subclass of AsyncTask that calls getFromLocation() in the
     * background. The class definition has these generic types:
     * Location - A Location object containing
     * the current location.
     * Void     - indicates that progress units are not used
     * String   - An address passed to onPostExecute()
     */
    private class GetAddressTask extends
            AsyncTask<Location, Void, String> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @params params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder =
                    new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(loc.getLatitude()) +
                        " , " +
                        Double.toString(loc.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }

        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(String address) {
            // Set activity indicator visibility to "gone"
            mActivityIndicator.setVisibility(View.GONE);
            // Display the results of the lookup.
            mCoordTextView.setText(address);
        }


    }


    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                     Log.w("LOCC", "CONNECTION_FAILURE_RESOLUTION_REQUEST RESULT_OK" );
                     // TODO
                     break;
                }
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }

        return false;
    }

}
