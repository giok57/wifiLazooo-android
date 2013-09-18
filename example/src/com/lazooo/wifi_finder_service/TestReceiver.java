package com.lazooo.wifi_finder_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class TestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int statusCode;
		statusCode = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
		if (statusCode == WifiManager.WIFI_STATE_ENABLED) {
			Intent serviceIntent = new Intent(context, LazoooWifiService.class);
			context.startService(serviceIntent);
		}
	}
}
