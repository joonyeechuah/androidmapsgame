package org.kealinghornets.jchuah.mapsapplication;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Player {
  Marker marker = null;
  String team = "none";
  String status = "alive";
  
  public Player(GoogleMap mMap) {
    LatLng moscow = new LatLng(55.755826,37.61733);

		this.marker = mMap.addMarker(new MarkerOptions().position(moscow));
  }
}
