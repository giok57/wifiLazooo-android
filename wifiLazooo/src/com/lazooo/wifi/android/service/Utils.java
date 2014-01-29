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
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import com.lazooo.wifi.android.application.util.Log;
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

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 24/01/14
 * Time: 17:18
 */
public class Utils {

    // Constants used for different security types
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

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
     * Support MAX one redirect
     * @param surl
     * @param returnNoConnection
     * @param returnNoRedirect
     * @return
     */
    public static String isRedirect(String surl, String returnNoConnection, String returnNoRedirect){
        String redirContent = getRedirContent(surl, "", 0, returnNoRedirect, returnNoConnection);

        return redirContent;
    }

    /**
     * @return The security of a given {@link ScanResult}.
     */
    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WEP, PSK, EAP };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }

    public static String normalizeUrl(final String taintedURL) throws MalformedURLException{
        final URL url;
        try
        {
            url = new URI(taintedURL).normalize().toURL();
        }
        catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }

        final String path = url.getPath().replace("/$", "");
        final SortedMap<String, String> params = createParameterMap(url.getQuery());
        final int port = url.getPort();
        final String queryString;

        if (params != null)
        {
            // Some params are only relevant for user tracking, so remove the most commons ones.
            for (Iterator<String> i = params.keySet().iterator(); i.hasNext();)
            {
                final String key = i.next();
                if (key.startsWith("utm_") || key.contains("session"))
                {
                    i.remove();
                }
            }
            queryString = "?" + canonicalize(params);
        }
        else
        {
            queryString = "";
        }

        return url.getProtocol() + "://" + url.getHost()
                + (port != -1 && port != 80 ? ":" + port : "")
                + path + queryString;
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

    private static String getRedirContent(String surl, String cookies, int redirects,
                                           String returnNoRedirect, String returnNoConnection){
        String redirect = "";
        boolean isRedirect = false;
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            surl = normalizeUrl(surl);
            url = new URL(surl);
            urlConnection = (HttpURLConnection) url.openConnection();
            if(urlConnection instanceof HttpsURLConnection){

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
                };

                // set the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sc.getSocketFactory());
                //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // set the all-trusting host verifier
                ((HttpsURLConnection) urlConnection).setHostnameVerifier(allHostsValid);

            }
            urlConnection.setRequestProperty("Cookie", cookies);
            urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            urlConnection.addRequestProperty("User-Agent", "Mozilla");

            urlConnection.getInputStream();
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    isRedirect = true;
            }
            if (isRedirect) {

                // get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");

                // get the cookie if need, for login
                cookies = urlConnection.getHeaderField("Set-Cookie");

                if(redirects <= 2){
                    redirect = getRedirContent(newUrl, cookies, (redirects + 1), returnNoRedirect, returnNoConnection);
                }else {
                    return null;
                }

            }else {

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (redirects == 0 && (url.toString().equals(urlConnection.getURL().toString()))) {
                    redirect = returnNoRedirect;
                }else {
                    StringBuilder response = new StringBuilder();
                    while ((redirect = br.readLine()) != null){
                        response.append(redirect);
                    }
                    redirect = response.toString();
                }
            }
        }catch (MalformedURLException mue){
            Log.e("SERVICE-UTILS", "malformed url exception");
        }catch (IOException ioe){
            if(redirects == 0){
                redirect = returnNoConnection;
            }else {
                redirect = "";
            }
        }catch (KeyManagementException kme){
            redirect = "";
        }catch (NoSuchAlgorithmException nsa){
            redirect = "";
        }
        finally{
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

                float f = 0.11765f / (TIMEOUT * 4);
                if(bandwidth < f){
                    return f;
                }else {
                    return bandwidth;
                }
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

    private static SortedMap<String, String> createParameterMap(final String queryString) {
        if (queryString == null || queryString.isEmpty())
        {
            return null;
        }

        final String[] pairs = queryString.split("&");
        final Map<String, String> params = new HashMap<String, String>(pairs.length);

        for (final String pair : pairs)
        {
            if (pair.length() < 1)
            {
                continue;
            }

            String[] tokens = pair.split("=", 2);
            for (int j = 0; j < tokens.length; j++)
            {
                try
                {
                    tokens[j] = URLDecoder.decode(tokens[j], "UTF-8");
                }
                catch (UnsupportedEncodingException ex)
                {
                    ex.printStackTrace();
                }
            }
            switch (tokens.length)
            {
                case 1:
                {
                    if (pair.charAt(0) == '=')
                    {
                        params.put("", tokens[0]);
                    }
                    else
                    {
                        params.put(tokens[0], "");
                    }
                    break;
                }
                case 2:
                {
                    params.put(tokens[0], tokens[1]);
                    break;
                }
            }
        }

        return new TreeMap<String, String>(params);
    }

    /**
     * Canonicalize the query string.
     *
     * @param sortedParamMap Parameter name-value pairs in lexicographical order.
     * @return Canonical form of query string.
     */
    private static String canonicalize(final SortedMap<String, String> sortedParamMap)
    {
        if (sortedParamMap == null || sortedParamMap.isEmpty())
        {
            return "";
        }

        final StringBuffer sb = new StringBuffer(350);
        final Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

        while (iter.hasNext())
        {
            final Map.Entry<String, String> pair = iter.next();
            sb.append(percentEncodeRfc3986(pair.getKey()));
            sb.append('=');
            sb.append(percentEncodeRfc3986(pair.getValue()));
            if (iter.hasNext())
            {
                sb.append('&');
            }
        }

        return sb.toString();
    }

    /**
     * Percent-encode values according the RFC 3986. The built-in Java URLEncoder does not encode
     * according to the RFC, so we make the extra replacements.
     *
     * @param string Decoded string.
     * @return Encoded string per RFC 3986.
     */
    private static String percentEncodeRfc3986(final String string)
    {
        try
        {
            return URLEncoder.encode(string, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        }
        catch (UnsupportedEncodingException e)
        {
            return string;
        }
    }
}
