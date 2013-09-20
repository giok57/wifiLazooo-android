package com.lazooo.wifi.android.application;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.example.R;

public class Main extends Activity {

    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);

    public void onCreate(Bundle savedBundle) {

        super.onCreate(savedBundle);
        setContentView(R.layout.map_test);

        GoogleMap map;
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
                                               .title("Hamburg"));
        Marker kiel = map.addMarker(new MarkerOptions()
                                            .position(KIEL)
                                            .title("Kiel")
                                            .snippet("Kiel is cool")
                                            .icon(BitmapDescriptorFactory
                                                          .fromResource(R.drawable.ic_launcher)));

    }

}
