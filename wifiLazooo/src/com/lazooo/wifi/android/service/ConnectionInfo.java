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

import android.net.wifi.WifiInfo;

/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 24/01/14
 * Time: 10:04
 */
public class ConnectionInfo {

    private WifiInfo wifiInfo;
    private boolean isConnected;
    private String SSID;
    private String MAC;
    private boolean isHidden;

    public ConnectionInfo(WifiInfo wifiInfo){
        this.wifiInfo = wifiInfo;
    }

    private void tryLoadIfConnected(){
        if (wifiInfo.getSSID().startsWith("\"") && wifiInfo.getSSID().endsWith("\"")){
            isConnected = true;
            SSID = wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length()-1);
            MAC = wifiInfo.getMacAddress();
            isHidden = wifiInfo.getHiddenSSID();
        }else {
            isConnected = false;
        }
    }
}
