package org.kealinghornets.jchuah.mapsapplication;


import org.kealinghornets.jchuah.mapsapplication.MapsFirebase.MapsFirebaseListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.os.Handler;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
    HashMap<String, CapturePoint> capturePoints = new HashMap<String, CapturePoint>();
  
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

		this.runOnUiThread(new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            showLoginDialog();
        }
        
      });
      mMap.setMyLocationEnabled(true);
      
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
    
  private Handler h = new Handler();
  
  private Runnable tick = new Runnable() {

    @Override
    public void run() {
      
      
      Toast.makeText(MapsFirebase.androidContext, "Admin Update", Toast.LENGTH_SHORT).show();
      
      if (!adminInitComplete) {
        adminInit();
      } else {
        for (String capture : capturePoints.keySet()) {
        	calcCapturePoint(capture);
        }
        respawnPlayers();
      }
      
      h.postDelayed(tick, 5000);
    }
    
  };
    
  private void respawnPlayers() {
    for (String playerId : players.keySet()) {
      Player p = players.get(playerId);
      if (p.team.equals("blue")) {
      	if (distance(p.marker.getPosition(), blueSpawn.getPosition()) < threshold) {
           MapsFirebase.setPlayerState(playerId, "alive");
         }
      }
      if (p.team.equals("red")) {
      	if (distance(p.marker.getPosition(), redSpawn.getPosition()) < threshold) {
           MapsFirebase.setPlayerState(playerId, "alive");
         }
      }
    }
  }
  
  private void adminStart() {
    admin = true;
    
    h.postDelayed(tick, 5000);
  }
    
    private int redscore = 0;
    private int bluescore = 0;
    private double boxBound = 0.0002;
    private double spawnDistance = 0.0001;
    private double threshold = 0.00005;
    private boolean admin = false;
    private boolean adminInitComplete = false;
    private LatLng myLocation = null;
    private int scoreThreshold = 50;
    
    private void adminInit() {
      if (myLocation != null) {
        MapsFirebase.setSpawnPoint("red", new LatLng(myLocation.latitude + spawnDistance, myLocation.longitude));
        MapsFirebase.setSpawnPoint("blue", new LatLng(myLocation.latitude - spawnDistance, myLocation.longitude));
        MapsFirebase.setCapturePoint("Capture Point 1", randomLocation());
        MapsFirebase.setCapturePoint("Capture Point 2", randomLocation());
        MapsFirebase.setCapturePoint("Capture Point 3", randomLocation());
        adminInitComplete = true;
      }
    }
    
    private void calcCapturePoint(String capture) {
      CapturePoint c = capturePoints.get(capture);
      LatLng p1 = c.marker.getPosition();
      
      ArrayList<String> nearPlayers = new ArrayList<String>();
      for (String playerId : players.keySet()) {
        if (players.get(playerId).status.equals("alive") && distance(p1, players.get(playerId).marker.getPosition()) < threshold) {
          nearPlayers.add(playerId);
        }
      }
      MapsFirebase.setPlayerState(nearPlayers.get(rand.nextInt(nearPlayers.size())), "dead");
      for (String playerId : nearPlayers) {
        if (players.get(playerId).team.equals("red")) {
          redscore++;
        }
        if (players.get(playerId).team.equals("blue")) {
          bluescore++;
        }
      }
      c.scoreCount += nearPlayers.size();
      MapsFirebase.setTeamScore("red", redscore);
      MapsFirebase.setTeamScore("blue", bluescore);
      if (c.scoreCount > scoreThreshold) {
        MapsFirebase.setCapturePoint(capture, randomLocation() );
      }
    }
    
    private double distance(LatLng p1, LatLng p2) {
      double x1 = p1.latitude;
      double x2 = p2.latitude;
      double y1 = p1.longitude;
      double y2 = p2.longitude;
      return Math.sqrt( Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
    
    private static Random rand = new Random();
    private LatLng randomLocation() {
      double latOffset = rand.nextDouble() * boxBound * 2;
      double longOffset = rand.nextDouble() * boxBound * 2;
      return new LatLng(myLocation.latitude - boxBound + latOffset, myLocation.longitude - boxBound + longOffset);
    }
    
    private void showLoginDialog() {
      		LayoutInflater li = LayoutInflater.from(this);
				View promptsView = li.inflate(R.layout.login, null);
 
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
 
				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);
 
				final EditText userInput = (EditText) promptsView
						.findViewById(R.id.editTextEmail);
      	   final Context context = this;
      		final MapsFirebaseListener fbListener = this;
 
				// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						// get user input and set it to result
						// edit text
						 MapsFirebase.startFirebase(context, fbListener, userInput.getText().toString(), "password");
						 if (MapsFirebase.myPlayerId().equals("simplelogin:17")) {
              				adminStart();
            			}
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
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
                                                       18.5f,
                                                       10,
                                                       l.getBearing()
                                    
                                                       );
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        if (MapsFirebase.authData != null) {
          MapsFirebase.setMyPosition(new LatLng(l.getLatitude(), l.getLongitude()));
        }
        myLocation = new LatLng(l.getLatitude(), l.getLongitude());
     
      
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
        capturePoints.put(capturePoint, new CapturePoint(mMap));
        capturePoints.get(capturePoint).marker.showInfoWindow();
      }
      capturePoints.get(capturePoint).marker.setPosition(position);
      
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
