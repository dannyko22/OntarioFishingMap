package com.ontariofishingmap.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

public class FishingMapsActivity extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    DataBaseHelper myDbHelper;
    ArrayList<FishData> fishList;
    Marker marker;
    private UiSettings mapUISetting;
    Hashtable<String, Integer> markers;
    private int mProgressStatus = 0;
    private ProgressDialog mProgressDialog ;
    private ProgressBarAsync mProgressbarAsync;
    InterstitialAd mInterstitialAd;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fishing_maps);

//        if (!isNetworkAvailable())
//        {
//            showAlertDialog();
//        }

        /** Creating a progress dialog window */
        mProgressDialog = new ProgressDialog(this);
        /** Close the dialog window on pressing back button */
        mProgressDialog.setCancelable(true);

        /** Setting a horizontal style progress bar */
        //mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        /** Setting a message for this progress dialog
         * Use the method setTitle(), for setting a title
         * for the dialog window
         *  */
        mProgressDialog.setMessage("Loading ... \n\nThis app requires an internet connection.\n\nIf you have a slow internet connection, please be patient.");

        /** Show the progress dialog window */
        mProgressDialog.show();

        /** Creating an instance of ProgressBarAsync */
        mProgressbarAsync = new ProgressBarAsync();

        /** ProgressBar starts its execution */
        mProgressbarAsync.execute();

        fishList = new ArrayList<FishData>();



        myDbHelper = new DataBaseHelper(this);
        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){

            throw sqle;
        }

        fishList = myDbHelper.getFishData();

        markers = new Hashtable<String, Integer>();
        setUpMapIfNeeded();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        //initializeAdNetwork();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Prepare the Interstitial Ad
//        interstitial = new InterstitialAd(this);
//// Insert the Ad Unit ID
//        interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));
//
//        interstitial.loadAd(adRequest);
//// Prepare an Interstitial Ad Listener
//        interstitial.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                // Call displayInterstitial() function
//                displayInterstitial();
//            }
//        });

    }


    private void initializeAdNetwork()
    {
        MMSDK.initialize(this);
        MMAdView adView = (MMAdView) findViewById(R.id.adView);

        //Replace YOUR_APID with the APID provided to you by Millennial Media
        adView.setApid("168006");

        //Set your metadata in the MMRequest object
        MMRequest request = new MMRequest();

        //Add the MMRequest object to your MMAdView.
        adView.setMMRequest(request);
        adView.getAd();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showAlertDialog()
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Sorry... This app requires an Internet connection.  :(");
        alertDialogBuilder.setNeutralButton("Exit the app", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                FishingMapsActivity.this.finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        int count = fishList.size();
        String name;
        double latitude;
        double longitude;
        String species;
        LatLng locationLatLngSetup;


        mapUISetting = mMap.getUiSettings();
        mapUISetting.setMyLocationButtonEnabled(true);
        mapUISetting.setTiltGesturesEnabled(false);
        mapUISetting.setRotateGesturesEnabled(false);
        mapUISetting.setCompassEnabled(true);

        mMap.setMyLocationEnabled(true);


        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            locationLatLngSetup = new LatLng(44.000559, -79.484977);
        } else { // Google Play Services are available
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location myLocation = locationManager.getLastKnownLocation(provider);

            if (myLocation != null) {
                if (myLocation.getLatitude() > 41 && myLocation.getLatitude() < 58 && myLocation.getLongitude() > -95.3 && myLocation.getLongitude() < -74.3) {
                    locationLatLngSetup = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                } else {
                    locationLatLngSetup = new LatLng(44.000559, -79.484977);
                }
            } else {
                locationLatLngSetup = new LatLng(44.000559, -79.484977);
            }
        }


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(locationLatLngSetup) // Sets the center of the map
                .zoom(9)                   // Sets the zoom
                .bearing(0) // Sets the orientation of the camera to north
                .tilt(0)    // Sets the tilt of the camera to 0 degrees
                .build();    // Creates a CameraPosition from the builder

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition));



        while (count != 0) {
            name = fishList.get(count-1).getName();
            latitude = Double.parseDouble(fishList.get(count-1).getLatitude());
            longitude = Double.parseDouble(fishList.get(count-1).getLongitude());
            species = fishList.get(count-1).getSpecies();
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name).snippet(species);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.fishing));
            Marker marker = mMap.addMarker(markerOptions);
            markers.put(marker.getId(), count-1);
            count--;
        }
        String text = "";
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String url = fishList.get(markers.get(marker.getId())).getURL();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window,null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (FishingMapsActivity.this.marker != null  && FishingMapsActivity.this.marker.isInfoWindowShown())
            {
                FishingMapsActivity.this.marker.hideInfoWindow();
                FishingMapsActivity.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            if (snippet.length() != 0)
            {
                snippet = snippet.substring(1);
                final TextView snippetUi = ((TextView) view
                        .findViewById(R.id.snippet));
                if (snippet != null) {
                    snippet = snippet.replaceAll("-", "\n");
                    snippet = snippet.replaceAll("_", " ");
                    snippetUi.setText(snippet);
                } else {
                    snippetUi.setText("");
                }
            }

            final String url = fishList.get(markers.get(marker.getId())).getURL();
            final TextView urlTextView = ((TextView) view
                    .findViewById(R.id.urlTextView));

            if (urlTextView != null) {
                urlTextView.setText(Html.fromHtml("<a href=\""+url+"\">Consumption Advisory</a>"));
                urlTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                urlTextView.setText("");
            }

            return view;
        }
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

        if (id == R.id.action_settings) {
            aboutMenuItem();
        }

        return super.onOptionsItemSelected(item);
    }

    private void aboutMenuItem() {
        startActivity(new Intent(this,about_me.class));

    }

    private class ProgressBarAsync extends AsyncTask<Void, Integer, Void> {

        /** This callback method is invoked, before starting the background process */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressStatus = 0;
        }

        /** This callback method is invoked on calling execute() method
         * on an instance of this class */
        @Override
        protected Void doInBackground(Void...params) {
            while(mProgressStatus<100){
                try{

                    mProgressStatus++;

                    /** Invokes the callback method onProgressUpdate */
                    //publishProgress(mProgressStatus);

                    /** Sleeps this thread for 100ms */
                    Thread.sleep(20);

                }catch(Exception e){
                    Log.d("Exception", e.toString());
                }
            }
            return null;
        }

        /** This callback method is invoked when publishProgress()
         * method is called */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(mProgressStatus);
        }

        /** This callback method is invoked when the background function
         * doInBackground() is executed completely */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
        }
    }

    public void displayInterstitial() {
// If Ads are loaded, show Interstitial else show nothing.
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }
}

