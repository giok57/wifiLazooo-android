package com.lazooo.wifi.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import com.google.gson.Gson;
import com.lazooo.wifi.android.application.Util;
import com.lazooo.wifi.android.application.util.Log;
import com.lazooo.wifi.android.application.util.Toast;

import java.util.*;

public class WifiLazoooService extends Service {

    private static final String LABEL = "SERVICE";
    private static final String CURRENT_WIFI = "last-connection";
    private static final String PENDING_WIFI = "pending-connection";
	private static final String WIFI_LAZOOO_SERVICE = "wifi-lazooo-service";
	private static final String SURL = "http://www.wikipedia.org";
	private static final String NO_REDIRECT = "!@@@ss@@@";
	private static final String NO_CONNECTION = "?@@@ss@@@";
    private static final int ALARM_EVERY = 3*10;

	private BroadcastReceiver wifiChangeReceiver;
	private BroadcastReceiver wifiScanResults;

	private SharedPreferences persistence;

    private WifiManager mWifi;
    private NetworkInfo nInfo;
    private ConnectivityManager mConn;
    private Gson gson;

	@Override
	public IBinder onBind(Intent arg0) {
		throw new NoSuchMethodError("Bind not implemented!");
	}

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        persistence = this.getSharedPreferences(WIFI_LAZOOO_SERVICE, MODE_PRIVATE);
        mWifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mConn = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        gson = new Gson();
        //////TEST
        persistence.edit().clear().commit();/////////////

        registerReceivers();
        Log.i(LABEL, "service created");
        Toast.showText(this, "service created");
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LABEL, "service onStartCommand");

        String connId = null;
        if((connId = intent.getStringExtra(CURRENT_WIFI)) != null){
            onMeasurament(connId, false);
        }
		return START_NOT_STICKY;
	}

    @Override
    public void onDestroy(){
        Log.i("[SERVICE]", "destroy");
        unregisterReceiver(wifiChangeReceiver);
        unregisterReceiver(wifiScanResults);
        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent intent){
        Log.i("[SERVICE]", "task removed");
        super.onTaskRemoved(intent);
    }

    public boolean checkInternetConnection() {
        if (mConn.getActiveNetworkInfo() != null && mConn.getActiveNetworkInfo().isAvailable()
                && mConn.getActiveNetworkInfo().isConnected()
                && Utils.intToIp(mWifi.getDhcpInfo().gateway).equals("0.0.0.0") == false){
            return true;

        }else{
            return false;
        }
    }

	private void registerReceivers() {
		//Receiver for on/off wifi toggle
		wifiChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int statusCode = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				if (statusCode == WifiManager.WIFI_STATE_DISABLED){
                    //stop everything
                    Log.i(LABEL, "service ending");
                    Toast.showText(context, "service ending");
                    stopSelf();
                }
			}
		};
		registerReceiver(this.wifiChangeReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		//Receiver for wifi scan
		wifiScanResults = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
                onScanAvailable();
			}
		};
		registerReceiver(this.wifiScanResults, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

    private void onScanAvailable(){
        try {
            String wifiId = "";
            if (mWifi.getConnectionInfo().getSSID().startsWith("\"") && mWifi.getConnectionInfo().getSSID().endsWith("\"")){
                int hash = mWifi.getConnectionInfo().getSSID().substring(1, mWifi.getConnectionInfo().getSSID().length() - 1)
                        .hashCode();
                wifiId = String.valueOf(hash).concat("C");
            }
            String currentWifiId = persistence.getString(CURRENT_WIFI, null);
            Log.e(LABEL, wifiId+"---"+currentWifiId);
            if(currentWifiId == null && checkInternetConnection() == true){

                //no current connection and new hotspot now connected
                Log.e(LABEL, "new");
                onConnectedWifi(wifiId);

            }else if(currentWifiId != null && checkInternetConnection() == false){

                //Ended previous hotspot connection
                Log.e(LABEL, "end");
                onDisconnectedWifi(currentWifiId);

            }else if(wifiId.equals(currentWifiId) == false && checkInternetConnection() == true){

                //Changed hotspot connection directly from the previous hotspot without disconnection
                Log.e(LABEL, "chg");
                onDisconnectedWifi(currentWifiId);
                onConnectedWifi(wifiId);

            }
        }catch (Exception exc){
            Log.e(LABEL, exc.toString() +" : "+ exc.getMessage());
        }
    }

    private void onConnectedWifi(String wifiId)throws Exception{
        List<ScanResult> networkList = mWifi.getScanResults();
        String connectedBSSID = mWifi.getConnectionInfo().getBSSID();

        String SSID = mWifi.getConnectionInfo().getSSID().substring(1, mWifi.getConnectionInfo()
                .getSSID().length()-1);
        String mac = mWifi.getConnectionInfo().getMacAddress();
        boolean isProtected = false;
        boolean done = false;

        if(connectedBSSID == null){
            throw new Exception("new hotspot connected but no BSSID");
        }
        if (networkList != null) {
            for (ScanResult network : networkList){
                if(connectedBSSID.equals(network.BSSID)){
                    //find type of hotspot's security
                    if(Utils.occurences(network.capabilities, "]") > 2){
                        isProtected = true;
                    }
                    done = true;
                }
            }
            if (!done){
                throw new Exception("Didn't found connected hotspot in scanned hotspots.");
            }
        }else {
            throw new Exception("new hotspot connected but no wifi in scanList");
        }

        String redirect = Utils.isRedirect(SURL, NO_CONNECTION, NO_REDIRECT);
        WifiBean wifiBean = null;
        if(redirect.equals(NO_CONNECTION)){
            wifiBean = new WifiBean(wifiId, isProtected, false, SSID, mac, "", 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false);
        }else if(redirect.equals(NO_REDIRECT)){
            wifiBean = new WifiBean(wifiId, isProtected, false, SSID, mac, "", 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false);
        }else {
            wifiBean = new WifiBean(wifiId, isProtected, true, SSID, mac, redirect, 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false);
        }
        Long time = System.currentTimeMillis();
        String connId = String.valueOf(SSID.concat(time.toString()).hashCode());
        String json = gson.toJson(new WifiHour(wifiBean, time, connId));

        persistence.edit().putString(CURRENT_WIFI, wifiId).commit();
        persistence.edit().putString(wifiId, json).commit();

        onMeasurament(connId, true);
    }

    @SuppressWarnings("unchecked")
    private void onDisconnectedWifi(String currentWifiId)throws Exception {

        String jsonCurrentWifi = persistence.getString(currentWifiId, null);
        if(jsonCurrentWifi == null){
            throw new Exception("on disconnection persistence inconsistent");
        }
        WifiHour wifiHour = gson.fromJson(jsonCurrentWifi, WifiHour.class);
        String json = persistence.getString(PENDING_WIFI, null);
        Set<String> pendingWifi = null;
        if(json == null){
            pendingWifi = new HashSet<String>();
        }else {
            pendingWifi = gson.fromJson(json, Set.class);
        }

        String newId = wifiHour.wifiBean.getWifiId().substring(0, wifiHour.wifiBean.getWifiId().length()-1);
        boolean existPrev = pendingWifi.contains(newId);
        long now = System.currentTimeMillis();
        if(existPrev){
            WifiHour prevWifi = gson.fromJson(persistence.getString(wifiHour.wifiBean.getWifiId(), ""), WifiHour.class);
            if(wifiHour.wifiBean.getConnTime() == 0){
                wifiHour.wifiBean.setConnTime(((int)(now - wifiHour.time) / 1000));
            }
            persistence.edit().putString(newId,
                    gson.toJson(Utils.betterWifi(wifiHour, prevWifi))).commit();
        }else {
            if(wifiHour.wifiBean.getConnTime() == 0){
                wifiHour.wifiBean.setConnTime(((int)(now - wifiHour.time) / 1000));
            }
            pendingWifi.add(newId);
            persistence.edit().putString(newId, gson.toJson(wifiHour)).commit();
        }
        json = gson.toJson(pendingWifi);

        persistence.edit().remove(CURRENT_WIFI).commit();
        persistence.edit().putString(PENDING_WIFI, json).commit();
    }

    private void onMeasurament(String connId, boolean first){
        String json = persistence.getString(connId, null);
        WifiHour wifiHour = null;
        if (json != null){
            wifiHour = gson.fromJson(json, WifiHour.class);
            if(connId.equals(wifiHour.connId)){
                float bandwidth = Utils.getBandwidth("https://ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojo/dojo.js");
                if(wifiHour.wifiBean.isInternet() == false && bandwidth > 0){
                    wifiHour.wifiBean.setInternet(true);
                    wifiHour.wifiBean.setConnSpeed(bandwidth);
                }
                if(wifiHour.wifiBean.getConnSpeed() < bandwidth){
                    wifiHour.wifiBean.setConnSpeed(bandwidth);
                }
                if(bandwidth >= 0 && first == false){
                    wifiHour.wifiBean.setConnTime(wifiHour.wifiBean.getConnTime() + ALARM_EVERY);
                }
                if(wifiHour.wifiBean.isInternet() == false && wifiHour.wifiBean.isRedirect() == false){
                    //re-check for redirect page or connection
                    String redirect = Utils.isRedirect(SURL, NO_CONNECTION, NO_REDIRECT);
                    if (redirect.equals(NO_REDIRECT)){
                        wifiHour.wifiBean.setRedirect(false);
                    }else if (redirect.equals(NO_CONNECTION)){
                        ;//leave the same
                    }else {
                        wifiHour.wifiBean.setRedirect(true);
                        wifiHour.wifiBean.setRedirectPage(redirect);
                    }
                }

                json = gson.toJson(wifiHour);
                persistence.edit().putString(wifiHour.wifiBean.getWifiId(), json).commit();
                Log.e(LABEL, wifiHour.wifiBean.toString());
                Intent intent = new Intent(this, WifiLazoooService.class);
                intent.putExtra(CURRENT_WIFI, wifiHour.wifiBean.getWifiId());
                PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                //alarm.set(AlarmManager.RTC_WAKEUP, (ALARM_EVERY*1000), pintent);
            }else {
                //missed some wifi changes
                onScanAvailable();
            }
        }
    }



    public class WifiHour{
        String connId;
        WifiBean wifiBean;
        long time;
        WifiHour(WifiBean wifiBean, long time, String connId){
            this.wifiBean = wifiBean;
            this.time = time;
            this.connId = connId;
        }
    }
}

