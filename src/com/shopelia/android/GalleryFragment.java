package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.shopelia.android.app.ShopeliaFragment;

/**
 * Created by pollas_p on 22/11/2013.
 */
public class GalleryFragment extends ShopeliaFragment<Void> {

    public static final String TAG = "Gallery";
    public static final DismissEvent DISMISS = new DismissEvent();

    public static class DismissEvent {
        private DismissEvent() {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shopelia_gallery_fragment, container, false);
        v.setOnTouchListener(mTouchAbsorber);
            //v.setVisibility(View.INVISIBLE);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setVisibility(View.VISIBLE);
        ValueAnimator zoomInX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1).setDuration(600);
        ValueAnimator zoomInY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1).setDuration(600);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(zoomInX, zoomInY);
        set.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivityEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    public void onEventMainThread(DismissEvent event) {
        ValueAnimator zoomOutX = ObjectAnimator.ofFloat(getView(), "scaleX", 1, 0).setDuration(600);
        ValueAnimator zoomOutY = ObjectAnimator.ofFloat(getView(), "scaleY", 1, 0).setDuration(600);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(zoomOutX, zoomOutY);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (getBaseActivity() != null) {
                    FragmentManager fm = getBaseActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.remove(GalleryFragment.this);
                    ft.commit();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.start();
    }


    private View.OnTouchListener mTouchAbsorber = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            return true;
        }
    };

}
