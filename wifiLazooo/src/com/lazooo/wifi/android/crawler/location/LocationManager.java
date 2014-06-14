package com.lazooo.wifi.android.crawler.location;/**
 * Lazooo copyright 2012
 */

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import com.lazooo.wifi.android.crawler.WifiLazoooService;

/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 29/01/14
 * Time: 09:43
 */
public class LocationManager {

    private PlayLocator playLocator;
    private ClassicLocator classicLocator;
    private WifiLazoooService wifiLazoooService;
    private long msToListen;
    private long time;

    private String wifiId;
    private Location currentBestLocation;

    public LocationManager(WifiLazoooService wifiLazoooService, int msToListen){

        this.msToListen = msToListen *1000;
        this.wifiLazoooService = wifiLazoooService;
        playLocator = new PlayLocator(this);
    }

    public void startUpdates(String wifiId){
        time = System.currentTimeMillis();
        this.wifiId = wifiId;
        Handler mainHandler = new Handler(wifiLazoooService.getMainLooper());
        Runnable runnable = new Runnable(){
            public void run(){
                playLocator.startUpdates();
            }
        };
        mainHandler.post(runnable);
    }

    public void stopUpdates(){
        Handler mainHandler = new Handler(wifiLazoooService.getMainLooper());
        Runnable runnable = new Runnable(){
            public void run(){
                playLocator.stopUpdates();
            }
        };
        time = 0;
        mainHandler.post(runnable);
        if (currentBestLocation != null && wifiId != null){
            wifiLazoooService.addWifiPosition(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(),
                    currentBestLocation.getAccuracy(), wifiId );
        }
        currentBestLocation = null;
        wifiId = null;

    }

    protected void onLocationChanged(Location location){
        isBetterLocation(location);
        long now = System.currentTimeMillis();
        if((now - time) > msToListen){
            stopUpdates();
        }
    }

    private void isBetterLocation(Location location) {
        if (currentBestLocation == null) {
            currentBestLocation = location;
        }else if(currentBestLocation.getAccuracy() >= location.getAccuracy()) {
            currentBestLocation = location;
        }
    }

    protected Context getContext(){
        return wifiLazoooService.getApplicationContext();
    }
}
