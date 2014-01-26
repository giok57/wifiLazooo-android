package com.lazooo.wifi.android.application.fragments;
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
 * Date: 25/09/13
 * Time: 12:03
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.location.Location;
import android.widget.Toast;
import com.google.android.gms.maps.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.lazooo.wifi.android.application.R;
import com.lazooo.wifi.android.application.util.CompatibilityUtil;


public class LazoooMapFragment extends SupportMapFragment {

    private static LatLng userPosition = null;
    private static float zoom = 19;
    /**
     *
     */
    public LazoooMapFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstance) {
        View layout = super.onCreateView(inflater, view, savedInstance);

        getMap().setMyLocationEnabled(true);
        if(userPosition == null){
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.121529, 12.393161), 5));
        }else{
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, zoom));
        }
        if(CompatibilityUtil.isHoneycomb() == true)
            //in newest android removes zoom buttons
            getMap().getUiSettings().setZoomControlsEnabled(false);

        Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.markerwifimidranklock);
        Bitmap resized = Bitmap.createScaledBitmap(icon, 32, 32, true);
        GroundOverlay groundOverlay = getMap().addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(icon))
                .position(new LatLng(43.115442,12.39015), 60, 60)
                .visible(true)
                .transparency(0.5f)
                .zIndex(200)
                );

        getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(userPosition != null){
                    //userposition setted on myPosition first time
                    userPosition = cameraPosition.target;
                    zoom = cameraPosition.zoom;
                }
            }
        });
        getMap().setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (userPosition == null) {
                    LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    userPosition = myLatLng;
                    getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 19));
                    zoom = 19;
                }
            }
        });


        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) layout).addView(frameLayout,
                new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return layout;
    }

    public static Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap, String gText) {

        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;

        android.graphics.Bitmap.Config bitmapConfig =   bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }

        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (40 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 2f, Color.WHITE);


        // draw text to the Canvas center
        Rect bounds = new Rect();

        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x * scale, y * scale, paint);

        return bitmap;
    }

}
