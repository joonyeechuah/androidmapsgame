

package org.kealinghornets.jchuah.mapsapplication;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.ResultHandler;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

public class MapsFirebase {
  static String firebaseURL = "https://burning-inferno-433.firebaseio.com/";
  static MapsFirebaseListener fbListener = null;
  
  
  static Firebase fbRef = null;
  static AuthData authData = null;
  static Context androidContext = null;
  static String username = "";
  static String password = "";
  
  
  public static void startFirebase(Context androidContext, MapsFirebaseListener fbListener, String username, String password) {
    MapsFirebase.androidContext = androidContext;
    MapsFirebase.fbListener = fbListener;
    Firebase.setAndroidContext(androidContext);
    fbRef = new Firebase(firebaseURL);
    
    /**
     * Added auto user creation
     */
    Log.d("Firebase Create User", "creating user " + username + " with password " + password);
    MapsFirebase.username = username;
    MapsFirebase.password = password;
    fbRef.createUser(username, password, new ResultHandler() {

        @Override
        public void onError(FirebaseError arg0) {
            // TODO Auto-generated method stub
            Log.d("Firebase Create User", "failure");
          	MapsFirebase.completeLogin();
        }

        @Override
        public void onSuccess() {
            // TODO Auto-generated method stub
            Log.d("Firebase Create User", "success");
          MapsFirebase.completeLogin();
        }
      
    });
    /**
     * End auto user creation
     */
    
    
    
  }
  
  private static void completeLogin() {
    Log.d("Firebase Complete Login", "Attempting to complete login");
		fbRef.authWithPassword(MapsFirebase.username, MapsFirebase.password,
        new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Authentication just completed successfully :)
            Log.d("MapsFirebaseAuth", "successful authorization");            
            for (String k : authData.getAuth().keySet()) {
            	Log.d("Auth Data", "key: " + k + " value: " + authData.getAuth().get(k));
	         }
            MapsFirebase.authData = authData;
            Log.d("Firebase Complete Login", "Authorized user");
            MapsFirebase.addListeners();
        }
        @Override
        public void onAuthenticationError(FirebaseError error) {
            // Something went wrong :(
            Log.d("Firebase Complete Login", "Authentication error");
        }
    });
    
    fbRef.addAuthStateListener(new Firebase.AuthStateListener() {
        @Override
        public void onAuthStateChanged(AuthData authData) {
            MapsFirebase.authData = authData;
        }
    });    
  }
  
  private static void addListeners() {
    
    fbRef.child("PlayerPositions").addValueEventListener(new ValueEventListener() {
        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub            
        }
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
			   for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String player = child.getKey();
                  Map<String, Double> values = (Map<String,Double>)child.getValue();
                  double lat = values.get("Latitude");
                  double longitude = values.get("Longitude");
                  String msg = "Player " + player + "(" + lat + ", " + longitude + ")";
                  MapsFirebase.fbListener.playerPositionUpdate(player, new LatLng(lat, longitude) );

                } catch (Exception e) {
                  Log.d("MapsFirebase PlayerPositions", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }      
    });
    fbRef.child("PlayerStatuses").addValueEventListener(new ValueEventListener() {
        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
          for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String player = child.getKey();
                  String status = (String)child.getValue();
                  MapsFirebase.fbListener.playerStatusUpdate(player, status);

                } catch (Exception e) {
                  Log.d("MapsFirebase PlayerStatuses", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }  
        }
    });
    fbRef.child("CapturePoints").addValueEventListener(new ValueEventListener() {

        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
            for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String capturePoint = child.getKey();
                  Map<String, Double> values = (Map<String,Double>)child.getValue();
                  double lat = values.get("Latitude");
                  double longitude = values.get("Longitude");
                  MapsFirebase.fbListener.capturePointUpdate(capturePoint, new LatLng(lat, longitude));

                } catch (Exception e) {
                  Log.d("MapsFirebase CapturePoints", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    });
    fbRef.child("TeamScores").addValueEventListener(new ValueEventListener() {

        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
				for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String team = child.getKey();
                  long score = (Long)child.getValue();
                  MapsFirebase.fbListener.teamScoreUpdate(team, (int)score);

                } catch (Exception e) {
                  Log.d("MapsFirebase TeamScores", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    });
    fbRef.child("TeamRoster").addValueEventListener(new ValueEventListener() {

        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
            for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String team = child.getKey();
                  ArrayList<String> playerList = (ArrayList<String>)child.getValue();
                  for (String player : playerList ) {
                    MapsFirebase.fbListener.teamRosterUpdate(player, team);
                  }

                } catch (Exception e) {
                  Log.d("MapsFirebase TeamRoster", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
      
    });    
    fbRef.child("SpawnPoints").addValueEventListener(new ValueEventListener() {

        @Override
        public void onCancelled(FirebaseError arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // TODO Auto-generated method stub
            for(DataSnapshot child : snapshot.getChildren()) {
                try {
                  String spawnPoint = child.getKey();
                  Map<String, Double> values = (Map<String,Double>)child.getValue();
                  double lat = values.get("Latitude");
                  double longitude = values.get("Longitude");
                  MapsFirebase.fbListener.spawnPointUpdate(spawnPoint, new LatLng(lat, longitude) );

                } catch (Exception e) {
                  Log.d("MapsFirebase SpawnPoints", "Exception: " + e.getMessage() + " snapshot: " + snapshot.getKey() + ": " + snapshot.getValue());
                  Toast.makeText(androidContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
      
    });
  }
  
  public static void setMyPosition(LatLng coordinate)  {
    if (MapsFirebase.isAuthorized()) {
      fbRef.child("PlayerPositions").child(MapsFirebase.myPlayerId()).child("Latitude").setValue(coordinate.latitude);
      fbRef.child("PlayerPositions").child(MapsFirebase.myPlayerId()).child("Longitude").setValue(coordinate.longitude);
    }
  }
  
  public static void setPlayerState(String playerId, String status) {
    if (MapsFirebase.isAuthorized()) {
      fbRef.child("PlayerStatuses").child(playerId).child("status").setValue(status);
    }
  }
  
  public static void setCapturePoint(String capturePoint, LatLng position) {
    if(MapsFirebase.isAuthorized()) {
      fbRef.child("CapturePoints").child(capturePoint).child("Latitude").setValue(position.latitude);
      fbRef.child("CapturePoints").child(capturePoint).child("Longitude").setValue(position.longitude);
    }
  }
  
  public static void assignPlayerToTeam(String playerId, String team) {
    if (MapsFirebase.isAuthorized()) {
      fbRef.child("TeamRoster").child(playerId).setValue(team);
    }
  }
  
  
  public static void setSpawnPoint(String team, LatLng position) {
    if (MapsFirebase.isAuthorized()) {
      fbRef.child("SpawnPoints").child(team).child("Latitude").setValue(position.latitude);
      fbRef.child("SpawnPoints").child(team).child("Longitude").setValue(position.longitude);
    };
  }
  
  public static void setTeamScore(String team, int score) {
    if (MapsFirebase.isAuthorized()) {
      fbRef.child("TeamScores").child(team).setValue(score);
    }
  }
  
  public static boolean isAuthorized() {
    return MapsFirebase.authData != null;
  }
  
  public static String myPlayerId() {
    if (MapsFirebase.authData == null) return "";
    return MapsFirebase.authData.getUid();
  }
  
  private static void snapshotDebug(String tag, DataSnapshot s) {
    
    Toast.makeText(androidContext, s.getValue().getClass().getSimpleName(), Toast.LENGTH_LONG).show();
  }
  
    
  public interface MapsFirebaseListener {
    public void playerPositionUpdate(String playerId, LatLng position);
    public void playerStatusUpdate(String playerId, String status);
    public void capturePointUpdate(String capturePoint, LatLng position);
    public void teamScoreUpdate(String team, int score);
    public void teamRosterUpdate(String playerId, String team);
    public void spawnPointUpdate(String team, LatLng position);
  }
  
}
