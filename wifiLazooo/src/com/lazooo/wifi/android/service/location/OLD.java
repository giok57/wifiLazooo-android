package com.lazooo.wifi.android.service.location;/**
 * Lazooo copyright 2012
 */

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import com.lazooo.wifi.android.application.util.Log;
import com.lazooo.wifi.android.service.WifiLazoooService;


/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 27/01/14
 * Time: 17:19
 */
public class OLD {

    private static long time;
    private static int MOVING_METERS = 30;
    private static Location currentBestLocation;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private WifiLazoooService service;

    private String currentWifiId;


    public OLD(WifiLazoooService service){
        // Acquire a reference to the system Location Manager
        this.locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
        this.service = service;

    }

    public void startLocationListener(final int secToListen, final String wifiId){

        this.currentWifiId = wifiId;
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.e("GPSSSSSSS", String.valueOf(location.getAccuracy()));
                // Called when a new location is found by the network location provider.
                isBetterLocation(location);

                long now = System.currentTimeMillis();
                if((now - time) > (secToListen * 1000)){
                    locationManager.removeUpdates(locationListener);
                    currentWifiId = null;
                    currentBestLocation = null;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        time = System.currentTimeMillis();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        final String best = locationManager.getBestProvider(criteria, true);
        Handler mainHandler = new Handler(service.getMainLooper());
        Runnable runnable = new Runnable(){
            public void run(){
                locationManager.requestLocationUpdates(best, 0, 0, locationListener);
            }
        };
        mainHandler.post(runnable);

    }

    public void stopLocationListener(){
        if(locationManager != null && locationListener != null){
            Handler mainHandler = new Handler(service.getMainLooper());
            Runnable runnable = new Runnable(){
                public void run(){
                    locationManager.removeUpdates(locationListener);
                }
            };
            mainHandler.post(runnable);
        }
        setNewLocationToWifi();
        currentWifiId = null;
        currentBestLocation = null;
    }

    private void setNewLocationToWifi(){
        if(currentBestLocation != null && currentWifiId != null){
            Log.e("FAVA", "settato");
            service.addWifiPosition(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(),
                    currentBestLocation.getAccuracy(), currentWifiId);
        }
    }

    protected void isBetterLocation(Location location) {
        if (currentBestLocation == null) {
            currentBestLocation = location;
        }else if(currentBestLocation.getAccuracy() >= location.getAccuracy()) {
            currentBestLocation = location;
        }
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
