package com.shopelia.android.widget.actionbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.view.animation.RotationTransition;

public class ActionBarWidget extends FrameLayout {

    private ViewGroup mOptionsContainer;
    private ViewGroup mBufferContainer;

    public ActionBarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        addView(inflater.inflate(R.layout.shopelia_action_bar, this, false));
        mOptionsContainer = (ViewGroup) findViewById(R.id.title_options_container_first);
        mBufferContainer = (ViewGroup) findViewById(R.id.title_options_container_second);
        swapBuffer();
    }

    public ViewGroup getOptionsContainer() {
        return mOptionsContainer;
    }

    public ViewGroup getBufferContainer() {
        return mBufferContainer;
    }

    public void swapBuffer() {
        ViewGroup tmp = mOptionsContainer;
        mOptionsContainer = mBufferContainer;
        mBufferContainer = tmp;
        mBufferContainer.setVisibility(INVISIBLE);
        mOptionsContainer.setVisibility(VISIBLE);
    }

    boolean d = false;

    public void swap() {
        ViewGroup tmp = mOptionsContainer;
        mOptionsContainer = mBufferContainer;
        mBufferContainer = tmp;
        if (mBufferContainer.getChildCount() == 0) {
            mOptionsContainer.setVisibility(View.VISIBLE);
            mBufferContainer.setVisibility(View.INVISIBLE);
        } else {
            new RotationTransition(mOptionsContainer, mBufferContainer, "rotationX").setDuration(
                    getResources().getInteger(R.integer.shopelia_animation_time_very_short)).start();
        }
    }
}
