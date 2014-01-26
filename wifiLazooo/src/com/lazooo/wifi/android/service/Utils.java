package com.lazooo.wifi.android.service;
/**
 The MIT License (MIT)

 Copyright (c) 2013 Lazooo

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 LazoooTeam
 */

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import com.lazooo.wifi.android.application.util.Log;
import com.lazooo.wifi.android.application.util.Toast;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.*;
import java.net.*;

/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 24/01/14
 * Time: 17:18
 */
public class Utils {

    private static final int MAXRETRIES = 3;
    private static final int TIMEOUT = 1000;


    public static String intToIp(int i) {

        return ((i >> 24 ) & 0xFF ) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ( i & 0xFF) ;
    }

    /**
     *
     * @param host the server to ping
     * @param args the url arguments and path (to concat with @host)
     * @return -1 if no connection available ( after @MAXRETRIES ), Mbps otherwise.
     */
    public static float getBandwidth(String ssurl){
        return getBandwidthInt(ssurl, 0);
    }

    /**
     *
     * @param surl
     * @param returnNoConnection
     * @param returnNoRedirect
     * @return
     */
    public static String isRedirect(String surl, String returnNoConnection, String returnNoRedirect){
        String redirect = null;
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(surl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            if (url.getHost().equals(urlConnection.getURL().getHost()) == false) {
                StringBuilder response = new StringBuilder();
                while ((redirect = br.readLine()) != null){
                    response.append(redirect);
                }
                redirect = response.toString();
            }else {
                redirect = returnNoRedirect;
            }
        }catch (MalformedURLException mue){
            Log.e("SERVICE-UTILS", "malformed url exception");
        }catch (IOException ioe){
            redirect = returnNoConnection;
        }finally{
            urlConnection.disconnect();
        }
        return redirect;
    }


    private static float getBandwidthInt(String ssurl, int repeat){
        long latencyTime = 0;
        Boolean reachable = false;
        URL url = null;
        try {
            url = new URL(ssurl);
        } catch (MalformedURLException e) {
            Log.e("SERVICE-UTILS", "malformed url in bandwidth");
        }
        int r = 0;
        for(int i = 0; i < 3; i++){
            //calculate the average latency of the network... stabilizing it for 5 times
            long BeforeTime = System.currentTimeMillis();
            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(TIMEOUT);
                urlConnection.connect();
                long AfterTime = System.currentTimeMillis();

                reachable = reachable || true;
                Long TimeDifference = AfterTime - BeforeTime;
                latencyTime += TimeDifference;
                r++;
            } catch (Exception e) {
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e1) {
                }
            }
        }

        if(reachable){
            //if network is reachable calculates the speed in Mbps without latencies
            latencyTime /= r;

            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(TIMEOUT);
                urlConnection.setReadTimeout(TIMEOUT*4);

                long startTime = System.currentTimeMillis() + latencyTime;

                byte[] bytes = IOUtils.toByteArray(urlConnection.getInputStream());

                long endTime = System.currentTimeMillis();

                long bits = bytes.length * 8;
                float kilobits = bits / (1000); //Kilobits
                float seconds = (endTime-startTime)/1000.0f;
                float bandwidth = ((kilobits/1000) / seconds);  //Megabits-per-second

                return bandwidth;
            } catch (IOException e) {
                return 0.11765f / (TIMEOUT * 4);
                /*if(repeat < MAXRETRIES){
                    return getBandwidthInt(ssurl, repeat + 1);
                }else {
                    return -1;
                }*/
            }
        }else {
            return -1;
        }
    }

    private static float normalizeConnTime(int connTime){
        if(connTime > 60*60){
            connTime = 60*60;
        }
        return connTime / 60*60;
    }

    private static float normalizeConnSpeed(float speed){
        if(speed > 10.0f){
            speed = 10.0f;
        }
        return speed / 10.0f;
    }

    public static int occurences(String string, String occurence){
        int nr = 0;
        for (int i = 0; i < string.length(); i++){
            if(string.substring(i, i + occurence.length()).equals(occurence)){
                nr++;
            }
        }
        return nr;
    }

    /**
     *
     * @param wifi1
     * @param wifi2
     * @return best hotspot from wifi1 e wifi2 with the oldest time of connection between them
     */
    public static WifiLazoooService.WifiHour betterWifi(WifiLazoooService.WifiHour wifi1,
                                                        WifiLazoooService.WifiHour wifi2){
        if (wifi1.wifiBean.isInternet() == true && wifi2.wifiBean.isInternet() == false){
            return wifi1;
        }
        if (wifi2.wifiBean.isInternet() == true && wifi1.wifiBean.isInternet() == false){
            return wifi2;
        }
        float weight1 = normalizeConnSpeed(wifi1.wifiBean.getConnSpeed()) + normalizeConnTime(wifi1.wifiBean.getConnTime());
        float weight2 = normalizeConnSpeed(wifi2.wifiBean.getConnSpeed()) + normalizeConnTime(wifi2.wifiBean.getConnTime());
        if(weight1 > weight2){
            if(wifi1.time > wifi2.time){
                wifi1.time = wifi2.time;
            }
            return wifi1;
        }else {
            if(wifi2.time > wifi1.time){
                wifi2.time = wifi1.time;
            }
            return wifi2;
        }
    }
}
