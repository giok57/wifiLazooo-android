package com.lazooo.wifi.android.application.fragments;


import android.content.res.Configuration;
import android.graphics.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import com.lazooo.wifi.android.application.R;
import com.lazooo.wifi.android.application.util.CompatibilityUtil;
import com.lazooo.wifi.android.application.util.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class LazoooNearWifiFragment extends Fragment {
	
	private int mColorRes = -1;
    private int mWifiNr = 0;

	public LazoooNearWifiFragment() {
		this(R.color.white);
	}
	
	public LazoooNearWifiFragment(int colorRes) {
		mColorRes = colorRes;
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null)
			mColorRes = savedInstanceState.getInt("mColorRes");
		int color = getResources().getColor(R.color.wifilazooo);

        int wifi2DisplayNr = -1;
        //Determine screen size
        if ((getActivity().getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            wifi2DisplayNr = 12;
        }
        else if ((getActivity().getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            wifi2DisplayNr = 6;
        }
        else if ((getActivity().getResources().getConfiguration().screenLayout &      Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            wifi2DisplayNr = 3;
        }
        else {
            wifi2DisplayNr = 12;
        }
        //display same way for every device screen
        int iconSize = CompatibilityUtil.getPxFromDp(getActivity(), 115);
        int iconCharge = CompatibilityUtil.getPxFromDp(getActivity(), 12);
        int cappuccinoWidth= CompatibilityUtil.getPxFromDp(getActivity(), 120);
        int cappuccinoHeight = CompatibilityUtil.getPxFromDp(getActivity(), 90);

        RelativeLayout rl = new RelativeLayout(getActivity());
        rl.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams params1;
        RelativeLayout.LayoutParams params2;



        final ImageView iv = new ImageView(getActivity());
        //iv.setImageResource(R.drawable.ringmidrank320x320);
        params1 = new RelativeLayout.LayoutParams(iconSize, iconSize);
        params1.leftMargin = 100;
        params1.topMargin = 60;



/**
        final RoundedImageView roundImage = new RoundedImageView(getActivity().getApplicationContext());
        roundImage.setVisibility(ImageView.VISIBLE);

        //setting image position
        roundImage.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        roundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        params2 = new RelativeLayout.LayoutParams(iconSize-iconCharge, iconSize-iconCharge);
        params2.leftMargin = 100+iconCharge/2;
        params2.topMargin = -60+iconCharge/2;
        rl.addView(roundImage, params2);
 */

        rl.addView(iv, params1);

        //ImageLoader.getInstance().displayImage("http://wifi.lazooo.com/images/wifiLazooo-device.jpg", imageView);
        // Load image, decode it to Bitmap and return Bitmap to callback
        ImageLoader.getInstance().loadImage("http://i1176.photobucket.com/albums/x340/BeautifulKayekie/PhotoEditorPicMonkeyEarrings.jpg", new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap loadedImage) {
                //loadedImage = getRoundedShape(loadedImage);
                //if(roundImage == null || loadedImage == null)
                //    return;
                //roundImage.setImageBitmap(loadedImage);
                iv.setImageBitmap(getCircleBitmap(loadedImage));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }

        });


        //make visible to program
        rl.setBackgroundColor(color);
        return rl;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mColorRes", mColorRes);
	}



    private Bitmap getCircleBitmap(Bitmap bitmap){

        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);

        Canvas c = new Canvas(circleBitmap);
        int smallDimension = 0;

        if(bitmap.getWidth() < bitmap.getHeight()){
            smallDimension = bitmap.getWidth();
        } else {
            smallDimension = bitmap.getHeight();
        }
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, smallDimension/2, paint);

        return circleBitmap;
    }

}
