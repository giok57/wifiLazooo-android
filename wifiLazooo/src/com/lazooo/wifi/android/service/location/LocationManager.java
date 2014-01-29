package com.lazooo.wifi.android.service.location;/**
 * Lazooo copyright 2012
 */

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.lazooo.wifi.android.service.WifiLazoooService;

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

    public LocationManager(WifiLazoooService wifiLazoooService){

        int playStatus = -1;
        if((playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(wifiLazoooService)) == ConnectionResult.SUCCESS){

        }
    }
}
