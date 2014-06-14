package com.lazooo.wifi.android.crawler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent serviceIntent = new Intent(context, WifiLazoooService.class);
		context.startService(serviceIntent);
	}

}
