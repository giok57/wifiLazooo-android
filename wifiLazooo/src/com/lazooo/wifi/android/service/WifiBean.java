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

/**
 * @author giok57
 * @email gioelemeoni@gmail.com
 * @modifiedBy giok57
 * <p/>
 * Date: 24/01/14
 * Time: 09:14
 */
public class WifiBean {
    private boolean isProtected;
    private boolean isRedirect;
    private String wifiId;
    private String ssid;
    private String mac;
    private String redirectPage;
    private int connTime;
    private boolean isInternet;
    private float connSpeed;
    private int signStr;

    public WifiBean(String wifiId, boolean isProtected, boolean isRedirect, String ssid, String mac, String redirectPage,
                    int connTime, float connSpeed, int signStr, boolean isInternet) {
        this.wifiId = wifiId;
        this.isProtected = isProtected;
        this.redirectPage = redirectPage;
        this.isRedirect = isRedirect;
        this.ssid = ssid;
        this.mac = mac;
        this.isInternet = isInternet;
        this.connTime = connTime;
        this.connSpeed = connSpeed;
        this.signStr = signStr;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getConnTime() {
        return connTime;
    }

    public void setConnTime(int connTime) {
        this.connTime = connTime;
    }

    public float getConnSpeed() {
        return connSpeed;
    }

    public void setConnSpeed(float connSpeed) {
        this.connSpeed = connSpeed;
    }

    public int getSignStr() {
        return signStr;
    }

    public void setSignStr(int signStr) {
        this.signStr = signStr;
    }

    public boolean isInternet() {
        return isInternet;
    }

    public void setInternet(boolean isInternet) {
        this.isInternet = isInternet;
    }

    public String getWifiId() {
        return wifiId;
    }

    public void setWifiId(String wifiId) {
        this.wifiId = wifiId;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean isRedirect) {
        this.isRedirect = isRedirect;
    }

    public String getRedirectPage() {
        return redirectPage;
    }

    public void setRedirectPage(String redirectPage) {
        this.redirectPage = redirectPage;
    }

    @Override
    public String toString() {
        return "WifiBean{" +
                "isProtected=" + isProtected +
                ", isRedirect=" + isRedirect +
                ", wifiId='" + wifiId + '\'' +
                ", ssid='" + ssid + '\'' +
                ", mac='" + mac + '\'' +
                ", connTime=" + connTime +
                ", isInternet=" + isInternet +
                ", connSpeed=" + connSpeed +
                ", signStr=" + signStr +
                '}';
    }
}
