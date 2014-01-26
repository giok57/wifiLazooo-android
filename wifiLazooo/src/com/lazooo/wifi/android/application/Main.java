package com.lazooo.wifi.android.application;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Main extends Activity {

    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);

    public void onCreate(Bundle savedBundle) {

        super.onCreate(savedBundle);
        setContentView(R.layout.map_test);

        GoogleMap map;
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker melbourne = map.addMarker(new MarkerOptions()
                .position(HAMBURG)
                .title("Melbourne")
                .snippet("Population: 4,137,400"));

        Marker kiel = map.addMarker(new MarkerOptions()
                                            .position(KIEL)
                                            .title("Kiel")
                                            .snippet("Kiel is cool")
                                            .icon(BitmapDescriptorFactory
                                                          .fromResource(R.drawable.wifilazooo_launcher_no)));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(53.551, 9.993), 10);
        map.animateCamera(cameraUpdate);

    }

}
