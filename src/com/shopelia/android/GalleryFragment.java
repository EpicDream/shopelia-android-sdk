package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shopelia.android.app.ShopeliaFragment;

/**
 * Created by pollas_p on 22/11/2013.
 */
public class GalleryFragment extends ShopeliaFragment<Void> {

    public static final String TAG = "Gallery";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shopelia_gallery_fragment, container, false);
        v.setOnTouchListener(mTouchAbsorber);
        return v;
    }


    private View.OnTouchListener mTouchAbsorber = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            return true;
        }
    };

}
