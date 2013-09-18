package com.lazooo.wifi_finder_service;

import android.app.Service;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.widget.Toast;

public class LazoooWifiService extends Service {

	private static final String CONNECTED_WIFI = "lazoooConnected";
	private static final String ONLY_SCANNED_WIFI = "lazoooOnlyScanned";

	private BroadcastReceiver wifiChangeReceiver;
	private BroadcastReceiver wifiScanResults;
	private SharedPreferences connectedWifi;
	private SharedPreferences onlyScannedWifi;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		connectedWifi = getSharedPreferences(CONNECTED_WIFI, 0);
		onlyScannedWifi = getSharedPreferences(ONLY_SCANNED_WIFI, 0);
		registerReceiver();
		Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_LONG).show();
		return START_STICKY;
	}

	private void registerReceiver() {
		//registrazione del broadcast receiver relativo allo spegnimento del wifi
		wifiChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int statusCode = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				if (statusCode == WifiManager.WIFI_STATE_DISABLED)
					stopSelf();
			}
		};
		registerReceiver(this.wifiChangeReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

		//registrazione del broadcast receiver relativo alla scansione
		wifiScanResults = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "trovata", Toast.LENGTH_SHORT).show();
			}
		};
		registerReceiver(this.wifiScanResults, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

}
