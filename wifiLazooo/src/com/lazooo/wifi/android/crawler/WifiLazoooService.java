package com.lazooo.wifi.android.crawler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.*;
import com.google.gson.Gson;
import com.lazooo.wifi.android.application.util.Log;
import com.lazooo.wifi.android.application.util.Toast;
import com.lazooo.wifi.android.crawler.location.LocationManager;

import java.util.*;

public class WifiLazoooService extends Service {

    //networking
    public static final String CRAWLER_ENDPOINT = "http://crawler.onlythebrave.lazooo.com/upload";
    public static final String S_CRAWLER_ENDPOINT = "https://crawler.onlythebrave.lazooo.com/upload";
    private static final String SURL = "http://www.wikipedia.org";
    private static final String SSURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojo/dojo.js";
    //persistence
    private static final String LABEL = "SERVICE";
    private static final String CURRENT_WIFI = "last-connection";
    private static final String READY_TO_SEND_WIFI = "ready-to-send-wifi";
    private static final String START_UPLOAD = "start-upload";
    private static final String PENDING_WIFI = "pending-connection";
	private static final String WIFI_LAZOOO_SERVICE = "wifi-lazooo-service";
	//private
    private static final int AROUNDS_NR = 10;
    private static final String NO_REDIRECT = "!@@@ss@@@";
	private static final String NO_CONNECTION = "?@@@ss@@@";
    //timing in seconds
    private static final int ALARM_EVERY = 3 * 60;
    private static final int READY_AFTER = 60 * 180;
    private static final int ONE_HOUR = 60 * 60;
    //objects
	private BroadcastReceiver wifiChangeReceiver;
	private BroadcastReceiver wifiScanResults;
	private SharedPreferences persistence;
    private LocationManager lManager;
    private WifiManager mWifi;
    private NetworkInfo nInfo;
    private ConnectivityManager mConn;
    private Gson gson;
    private WifiLazoooService wifiLazoooService;
    private String connected;

	@Override
	public IBinder onBind(Intent arg0) {

		throw new NoSuchMethodError("Bind not implemented!");
	}

    @Override
    public void onCreate() {

        super.onCreate();
        this.connected = null;
        this.wifiLazoooService = this;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        lManager = new LocationManager(this, 12);
        persistence = this.getSharedPreferences(WIFI_LAZOOO_SERVICE, MODE_PRIVATE);
        //DEBUG//
        persistence.edit().clear().commit();
        //END DEBUG//
        mWifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mConn = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        gson = new Gson();
        registerReceivers();
        Log.i(LABEL, "service created");
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {

        String wifiId = null;
        if(persistence.getString(CURRENT_WIFI, "").equals((wifiId = intent.getStringExtra(CURRENT_WIFI)))){

            //ALARM for new measuration of wifi statistics
            Log.e("SERVICE-ALARM","alarm started " + wifiId);
            new ScanAvailableTask(wifiLazoooService).execute(true);
        }else if((wifiId = intent.getStringExtra(READY_TO_SEND_WIFI)) != null){

            //ALARM for set wifi ready (every 3 hours)
            Log.e("SERVICE-ALARM-READY","alarm started " + wifiId);
            String json = persistence.getString(PENDING_WIFI, null);
            boolean ready = false;
            if (json != null){

                Set<String> pendingWifi = gson.fromJson(json, Set.class);
                String newWifiId = wifiId.substring(0, wifiId.length()-1);
                if(pendingWifi.contains(wifiId)){
                    //set the "ready" label to the wifi
                    json = persistence.getString(newWifiId, null);
                    WifiHour wifiHour = gson.fromJson(json, WifiHour.class);
                    wifiHour.ready = true;
                    persistence.edit().putString(newWifiId, gson.toJson(wifiHour)).commit();
                    ready = true;
                }
                }
            if (ready == false){

                //postpone ONE hour
                intent = new Intent(this, WifiLazoooService.class);
                intent.putExtra(READY_TO_SEND_WIFI, wifiId);
                PendingIntent pintent = PendingIntent.getService(this, wifiId.hashCode(), intent, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC, (System.currentTimeMillis() + (ONE_HOUR * 1000)), pintent);
            }
        }else if(START_UPLOAD.equals(intent.getStringExtra(START_UPLOAD))){

            //ALARM to start server upload
            long alarm = System.currentTimeMillis() + ONE_HOUR;
            persistence.edit().putLong(START_UPLOAD, alarm);
            tryToUploadPending(false);
        }
        long alarm = persistence.getLong(START_UPLOAD, -1);
        if(alarm == -1){

            alarm = System.currentTimeMillis() + ONE_HOUR * 1000;
            persistence.edit().putLong(START_UPLOAD, alarm);
        }
        intent = new Intent(this, WifiLazoooService.class);
        intent.putExtra(START_UPLOAD, START_UPLOAD);
        PendingIntent pi = PendingIntent.getService(this, 21212, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, alarm, pi);
        return START_NOT_STICKY;
	}

    @Override
    public void onDestroy(){

        Log.i("[SERVICE]", "destroy");
        unregisterReceiver(wifiChangeReceiver);
        unregisterReceiver(wifiScanResults);
        lManager.stopUpdates();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent intent){

        Log.i("[SERVICE]", "task removed");
        unregisterReceiver(wifiChangeReceiver);
        unregisterReceiver(wifiScanResults);
        lManager.stopUpdates();
        if(Build.VERSION.SDK_INT >= 14){

            super.onTaskRemoved(intent);
        }
    }

    public boolean isNetworkConnected() {

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
                    Log.d(LABEL, "service ending");
                    new ScanAvailableTask(wifiLazoooService).execute(false);
                }
			}
		};
		registerReceiver(this.wifiChangeReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		//Receiver for wifi scan
		wifiScanResults = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
                new ScanAvailableTask(wifiLazoooService).execute(false);
			}
		};
		registerReceiver(this.wifiScanResults, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

    private void onScanAvailable(boolean alarm){

        try {

            String wifiId = "";
            if (mWifi.getConnectionInfo().getSSID().startsWith("\"")
                    && mWifi.getConnectionInfo().getSSID().endsWith("\"")){

                int hash = mWifi.getConnectionInfo().getSSID().substring(1,
                        mWifi.getConnectionInfo().getSSID().length() - 1)
                        .hashCode();
                wifiId = String.valueOf(hash).concat("C");
            }
            String currentWifiId = persistence.getString(CURRENT_WIFI, null);
            //Log.e(LABEL, wifiId+" - "+currentWifiId);
            if(currentWifiId == null && isNetworkConnected() == true){

                //no current connection and new hotspot now connected
                Log.e(LABEL, "new");
                onConnectedWifi(wifiId);
            }else if(currentWifiId != null && isNetworkConnected() == false){

                //Ended previous hotspot connection
                Log.e(LABEL, "end");
                onDisconnectedWifi(currentWifiId);
                stopSelf();
            }else if(wifiId.equals(currentWifiId) == false && isNetworkConnected() == true){

                //Changed hotspot connection directly from the previous hotspot without disconnection
                Log.e(LABEL, "chg");
                onDisconnectedWifi(currentWifiId);
                onConnectedWifi(wifiId);
            }else if(wifiId.equals(currentWifiId) && isNetworkConnected() == true && alarm){

                //on a refresh called by the AlarmManager
                Log.e(LABEL, "measure");
                onMeasurament(wifiId, false);
            }else if(currentWifiId == null && isNetworkConnected() == false){
                //stopSelf()
                stopSelf();
            }
        }catch (Exception exc){

            Log.e(LABEL, exc.toString() +" : "+ exc.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void onConnectedWifi(String wifiId) throws Exception {

        connected = wifiId;
        String json = persistence.getString(PENDING_WIFI, null);
        boolean existPrev = false;
        Set<String> pendingWifi = null;
        if(json != null){

            pendingWifi = gson.fromJson(json, Set.class);
            if(pendingWifi.contains(wifiId)){

                Intent intent = new Intent(this, WifiLazoooService.class);
                intent.putExtra(READY_TO_SEND_WIFI, wifiId);
                PendingIntent pintent = PendingIntent.getService(this, wifiId.hashCode(), intent, 0);
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC, (System.currentTimeMillis() + READY_AFTER * 1000), pintent);
            }
        }
        List<ScanResult> networkList = mWifi.getScanResults();
        String connectedBSSID = mWifi.getConnectionInfo().getBSSID();
        String SSID = mWifi.getConnectionInfo().getSSID().substring(1, mWifi.getConnectionInfo()
                .getSSID().length()-1);
        String mac = mWifi.getConnectionInfo().getBSSID();
        String security = "";
        boolean done = false;
        Set<String> wifiAround = new HashSet<String>();
        if(connectedBSSID == null){

            throw new Exception("new hotspot connected but no BSSID");
        }
        if (networkList != null) {

            int nr = 0;
            String sec = "";
            for (ScanResult network : networkList){

                sec = Utils.getScanResultSecurity(network);
                if(nr < AROUNDS_NR && sec.equals(Utils.OPEN) && (connectedBSSID.equals(network.BSSID) == false)){

                    if (network.SSID.equals("") == false){

                        wifiAround.add(network.SSID);
                        nr++;
                    }
                }else if(connectedBSSID.equals(network.BSSID)){

                    //find type of hotspot's security
                    security = sec;
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
        Set<String> macs = new HashSet<String>();
        macs.add(mac);
        if(redirect.equals(NO_CONNECTION)){

            wifiBean = new WifiBean(0, 0, wifiId, security, false, SSID, macs, wifiAround, "", 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false, new HashSet<WifiBean.Location>());
        }else if(redirect.equals(NO_REDIRECT)){

            wifiBean = new WifiBean(0, 0, wifiId, security, false, SSID, macs, wifiAround, "", 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false, new HashSet<WifiBean.Location>());
        }else {

            wifiBean = new WifiBean(0, 0, wifiId, security, true, SSID, macs, wifiAround, redirect, 0, -1,
                    mWifi.getConnectionInfo().getRssi(), false, new HashSet<WifiBean.Location>());
        }
        Long time = System.currentTimeMillis();
        if (wifiId.equals(connected)){

            String connId = String.valueOf(SSID.concat(time.toString()).hashCode());
            json = gson.toJson(new WifiHour(wifiBean, time, connId));
            persistence.edit().putString(CURRENT_WIFI, wifiId).commit();
            persistence.edit().putString(wifiId, json).commit();
            onMeasurament(wifiId, true);
        }
    }

    @SuppressWarnings("unchecked")
    private void onDisconnectedWifi(String currentWifiId)throws Exception {
        connected = null;
        lManager.stopUpdates();
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
        String oldId = wifiHour.wifiBean.getWifiId();
        String newId = oldId.substring(0, oldId.length()-1);
        wifiHour.wifiBean.setWifiId(newId);
        boolean existPrev = pendingWifi.contains(newId);
        long now = System.currentTimeMillis();
        if(existPrev){

            WifiHour prevWifi = gson.fromJson(persistence.getString(wifiHour.wifiBean.getWifiId(), ""), WifiHour.class);
            if(wifiHour.wifiBean.getConnTime() == 0){

                wifiHour.wifiBean.setConnTime(((int)(now - wifiHour.time) / 1000));
            }
            persistence.edit().putString(newId,
                    gson.toJson(Utils.betterWifi(wifiHour, prevWifi))).commit();
            Log.e(LABEL, " exists prew similar hotspot");
        }else {

            if(wifiHour.wifiBean.getConnTime() == 0){

                wifiHour.wifiBean.setConnTime(((int)(now - wifiHour.time) / 1000));
            }
            pendingWifi.add(newId);
            Log.e(LABEL, " NOT exists prew similar hotspot");
            persistence.edit().putString(newId, gson.toJson(wifiHour)).commit();
        }
        json = gson.toJson(pendingWifi);
        persistence.edit().remove(CURRENT_WIFI).commit();
        persistence.edit().remove(oldId).commit();
        persistence.edit().putString(PENDING_WIFI, json).commit();
    }

    private void onMeasurament(String wifiId, boolean first){

        String json = persistence.getString(wifiId, null);
        WifiHour wifiHour = null;
        if (json != null){

            wifiHour = gson.fromJson(json, WifiHour.class);
            if(wifiId.equals(wifiHour.wifiBean.getWifiId())){

                connected = wifiId;
                //re-aproximate location of hotspot
                lManager.stopUpdates();
                lManager.startUpdates(wifiId);
                float bandwidth = Utils.getBandwidth(SSURL, this);
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
                if(mWifi.getConnectionInfo().getBSSID() != null){

                    wifiHour.wifiBean.getMacs().add(mWifi.getConnectionInfo().getBSSID());
                }
                List<ScanResult> networkList = mWifi.getScanResults();
                if (networkList != null) {

                    int nr = 0;
                    String sec = "";
                    Set<String> oldWifiAround = wifiHour.wifiBean.getWifiAround();
                    wifiHour.wifiBean.setWifiAround(new HashSet<String>());
                    String aSSID = "";
                    for (ScanResult network : networkList){

                        sec = Utils.getScanResultSecurity(network);
                        if (network.SSID.equals("") == false){

                            aSSID = network.SSID;
                            if(sec.equals(Utils.OPEN) && oldWifiAround.contains(aSSID)){

                                wifiHour.wifiBean.getWifiAround().add(aSSID);
                            }
                        }
                    }
                }
                if(wifiId.equals(connected)){

                    json = persistence.getString(wifiHour.wifiBean.getWifiId(), null);
                    WifiHour old = null;
                    if (json != null){

                        old = gson.fromJson(json, WifiHour.class);
                        if (old != null){

                            wifiHour.wifiBean.setLocations(old.wifiBean.getLocations());
                        }else {

                            //BUG cause connected is ok
                            Log.e(LABEL, "BUG INSIDE onMeasuraments()");
                        }
                    }
                    json = gson.toJson(wifiHour);
                    persistence.edit().putString(wifiHour.wifiBean.getWifiId(), json).commit();
                    if(wifiHour.wifiBean.getConnTime() < ONE_HOUR){

                        //debug//
                        final WifiHour wifiHour1 = wifiHour;
                        Handler mainHandler = new Handler(wifiLazoooService.getMainLooper());
                        Runnable runnable = new Runnable(){
                            public void run(){
                                Toast.showText(wifiLazoooService, wifiHour1.wifiBean.getSsid());
                            }
                        };
                        mainHandler.post(runnable);
                        Log.e(LABEL, wifiHour.toString());
                        //end debug//

                        Intent intent = new Intent(this, WifiLazoooService.class);
                        intent.putExtra(CURRENT_WIFI, wifiHour.wifiBean.getWifiId());
                        PendingIntent pintent = PendingIntent.getService(this, wifiId.hashCode(), intent, 0);
                        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        alarm.set(AlarmManager.RTC, (System.currentTimeMillis() + ALARM_EVERY*1000), pintent);
                    }
                }
            }else {
                //missed some wifi changes
                new ScanAvailableTask(this).execute();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void tryToUploadPending(boolean inWifi){
        if(persistence.getBoolean("send-3G", false) || inWifi) {

            String json = persistence.getString(PENDING_WIFI, null);
            List<WifiHour> list = null;
            if(json != null){

                WifiHour wifiHour = null;
                list = new LinkedList<WifiHour>();
                Set<String> pending = gson.fromJson(json, Set.class);
                for (String wifiId : pending){

                    json = persistence.getString(wifiId, null);
                    if(json != null){

                        wifiHour = gson.fromJson(json, WifiHour.class);
                        if (wifiHour.ready){

                            list.add(wifiHour);
                        }
                    }
                }
                if(list.size() > 0){

                    list = Utils.uploadJson(list);
                    for (WifiHour wifiUploaded : list){

                        pending.remove(wifiUploaded.wifiBean.getWifiId());
                        persistence.edit().remove(wifiUploaded.wifiBean.getWifiId()).commit();
                    }
                    persistence.edit().putString(PENDING_WIFI, gson.toJson(pending)).commit();
                }
            }
        }
    }

    public void setUserIsMoving(boolean moving, String wifiId){
        String json = persistence.getString(wifiId, null);
        if(json != null){

            WifiHour wifiHour = gson.fromJson(json, WifiHour.class);
            wifiHour.userMoving = moving;
            json = gson.toJson(wifiHour);
            persistence.edit().putString(wifiId, json).commit();
        }
    }

    public void addWifiPosition(double lat, double lon, float quality, String wifiId){
        String json = persistence.getString(wifiId, null);
        if(json != null){

            WifiHour wifiHour = gson.fromJson(json, WifiHour.class);
            wifiHour.wifiBean.getLocations().add(new WifiBean.Location(lat, lon, quality));
            json = gson.toJson(wifiHour);
            persistence.edit().putString(wifiId, json).commit();
        }
    }

    public WifiHour getWifi(String wifiId){
        String json = persistence.getString(wifiId, null);
        if(json != null){

            WifiHour wifiHour = gson.fromJson(json, WifiHour.class);
            return wifiHour;
        }
        return null;
    }

    /**
     *
     *
     */
    protected class ScanAvailableTask extends AsyncTask<Boolean, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        WifiLazoooService wifiLazoooService;

        // Constructor called by the system to instantiate the task
        public ScanAvailableTask(WifiLazoooService wifiLazoooService) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            this.wifiLazoooService = wifiLazoooService;
        }

        @Override
        protected String doInBackground(Boolean... params) {

            wifiLazoooService.onScanAvailable(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String address) {

        }


    }


    public class WifiHour {

        boolean ready;
        boolean userMoving;
        String connId;
        WifiBean wifiBean;
        long time;

        WifiHour(WifiBean wifiBean, long time, String connId){

            this.ready = false;
            this.wifiBean = wifiBean;
            this.time = time;
            this.connId = connId;
            this.userMoving = false;
        }

        @Override
        public String toString() {
            return "WifiHour{" +
                    "ready=" + ready +
                    ", userMoving=" + userMoving +
                    ", connId='" + connId + '\'' +
                    ", wifiBean=" + wifiBean +
                    ", time=" + time +
                    '}';
        }
    }
}

