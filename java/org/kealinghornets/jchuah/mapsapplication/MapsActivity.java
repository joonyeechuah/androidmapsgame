package org.kealinghornets.jchuah.mapsapplication;


import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

import android.support.v4.app.FragmentActivity;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, 
  GoogleApiClient.OnConnectionFailedListener, LocationListener, MapsFirebase.MapsFirebaseListener {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
  	 private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
	 private boolean mRequestingLocationUpdates = true;
    private boolean mResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    
    HashMap<String, Player> players = new HashMap<String, Player>();
    Marker blueSpawn = null;
    Marker redSpawn = null;
    HashMap<String, Marker> capturePoints = new HashMap<String, Marker>();
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	     mResolvingError = savedInstanceState != null
            && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        createLocationRequest();
        setContentView(R.layout.fragment_maps);
        buildGoogleApiClient();
        setUpMapIfNeeded();
    }


    protected synchronized void buildGoogleApiClient() {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
    } 
  


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    		setUpMapIfNeeded();
    }
    
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    
    private void setUpMap() {
		blueSpawn = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).icon(BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_BLUE)).title("Blue Spawn"));
      blueSpawn.showInfoWindow();
      redSpawn = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).icon(BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_RED)).title("Red Spawn"));        
        redSpawn.showInfoWindow();
      
      MapsFirebase.startFirebase(this, this, "user", "password");
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
           
        }
    	  if (mRequestingLocationUpdates) {
        		startLocationUpdates(); 
        }        
    }
  
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        } 
        
        
    }
    
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }
    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapsActivity)getActivity()).onDialogDismissed();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLocationChanged(Location l) {
        // TODO Auto-generated method stub
        mCurrentLocation = l;
        CameraPosition newPosition = new CameraPosition(new LatLng( l.getLatitude(), l.getLongitude()),
                                                       10,
                                                       10,
                                                       l.getBearing()
                                    
                                                       );
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        MapsFirebase.setMyPosition(new LatLng(l.getLatitude(), l.getLongitude()));
      
    }

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }
    
    public void playerPositionUpdate(String playerId, LatLng position) {
      if (!players.containsKey(playerId)) {
        players.put(playerId, new Player(mMap));
      }
      players.get(playerId).marker.setPosition(position);      
    }
    
    public void playerStatusUpdate(String playerId, String status) {
      if (playerId.equals(MapsFirebase.myPlayerId())) {
        if (status.equals("dead")) {
          Toast.makeText(this, "YOU DIED. GO RESPAWN.", Toast.LENGTH_LONG).show();
        }
        if (status.equals("alive")) {
          Toast.makeText(this, "YOU ARE ALIVE. GO DOMINATE!!!!.", Toast.LENGTH_LONG).show();
        }
      }
      if (!players.containsKey(playerId)) {
        players.put(playerId, new Player(mMap));
      }
      if (status.equals("dead")) {
	      players.get(playerId).marker.setAlpha(0.5f);
      }
      if (status.equals("alive")) {
         players.get(playerId).marker.setAlpha(1.0f);
      }
    }
    public void capturePointUpdate(String capturePoint, LatLng position) {
      if (!capturePoints.containsKey(capturePoint)) {
        capturePoints.put(capturePoint, mMap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_GREEN)).title(capturePoint)));
        capturePoints.get(capturePoint).showInfoWindow();
      }
      capturePoints.get(capturePoint).setPosition(position);
      
    }
    public void teamScoreUpdate(String team, int score) {
      if (team.equals("blue")) {
        blueSpawn.setSnippet("Score: " + score);
      }
      if (team.equals("red")) {
        redSpawn.setSnippet("Score: " + score);
      }
      
    }
    public void teamRosterUpdate(String playerId, String team) {
		if (!players.containsKey(playerId)) {
        players.put(playerId, new Player(mMap));
      }
      if (team.equals("red")) {
	      players.get(playerId).marker.setIcon(
           BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_RED));
      }
      if (team.equals("blue")) {
	      players.get(playerId).marker.setIcon(
           BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_BLUE));        
      }
    }
    public void spawnPointUpdate(String team, LatLng position) {
      if (team.equals("blue")) {
        blueSpawn.setPosition(position);
      }
      if (team.equals("red")) {
        redSpawn.setPosition(position);
      }
    }
    
}
