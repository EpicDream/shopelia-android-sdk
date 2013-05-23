package com.shopelia.android.widget.actionbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shopelia.android.R;

public class ActionBarWidget extends FrameLayout {

    private ViewGroup mOptionsContainer;

    public ActionBarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        addView(inflater.inflate(R.layout.shopelia_action_bar, this, false));
        mOptionsContainer = (ViewGroup) findViewById(R.id.title_options_container);
    }

    public ViewGroup getOptionsContainer() {
        return mOptionsContainer;
    }

}
