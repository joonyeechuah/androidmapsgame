package org.kealinghornets.jchuah.mapsapplication;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class CapturePoint {
  public Marker marker = null;
  public int scoreCount = 0;
  
  public CapturePoint(GoogleMap mMap) {
    LatLng moscow = new LatLng(55.755826,37.61733);

		this.marker = mMap.addMarker(new MarkerOptions().position(moscow).icon(BitmapDescriptorFactory.defaultMarker(
             BitmapDescriptorFactory.HUE_GREEN)).title("Capture Point"));
  }
}
