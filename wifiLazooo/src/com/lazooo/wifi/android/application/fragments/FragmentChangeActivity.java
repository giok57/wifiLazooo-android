package com.lazooo.wifi.android.application.fragments;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.lazooo.wifi.android.application.BaseActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lazooo.wifi.android.application.R;
import com.squareup.picasso.Picasso;

public class FragmentChangeActivity extends BaseActivity {
	
	private Fragment mContent;
    private Fragment mMapFragment;

	public FragmentChangeActivity() {
		super(R.string.changing_fragments);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //start service

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new LazoooNearWifiFragment(R.color.red);

		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new ColorMenuFragment())
		.commit();
		
		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}
	
	public void switchContent(Fragment fragment) {
        if(mContent instanceof LazoooMapFragment){
            mMapFragment = mContent;
            if(fragment instanceof LazoooMapFragment == false){
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(mMapFragment)
                        .add(R.id.content_frame, fragment)
                        .commit();
                mContent = fragment;
            }
        }
        else if(fragment instanceof  LazoooMapFragment){
            if(mMapFragment == null){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
                mContent = fragment;
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(mContent)
                        .show(mMapFragment)
                        .commit();
                mContent = mMapFragment;
            }
        } else{
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mContent)
                    .add(R.id.content_frame, fragment)
                    .commit();
            mContent = fragment;
        }
        getSlidingMenu().showContent();
	}

    /**
     *
     * @param fragment
     * @param position if position == -1 -> replase
     */
    public void switchContent(Fragment fragment, Integer position) {
        if(mContent instanceof LazoooMapFragment){
            mMapFragment = mContent;
            if(fragment instanceof LazoooMapFragment == false){
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(mMapFragment)
                        .add(R.id.content_frame, fragment)
                        .commit();
                mContent = fragment;
            }
        }
        else if(fragment instanceof  LazoooMapFragment){
            if(mMapFragment == null){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
                mContent = fragment;
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(mContent)
                        .show(mMapFragment)
                        .commit();
                mContent = mMapFragment;
            }
        } else{
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mContent)
                    .add(R.id.content_frame, fragment)
                    .commit();
            mContent = fragment;
        }
        getSlidingMenu().showContent();
    }
}
