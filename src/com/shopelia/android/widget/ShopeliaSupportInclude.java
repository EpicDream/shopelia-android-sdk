package com.shopelia.android.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shopelia.android.R;

public class ShopeliaSupportInclude extends FrameLayout {

    private View mView;

    public ShopeliaSupportInclude(Context context, AttributeSet attrs) {
        super(context, attrs);
        selfInflation();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.shopelia_include_company_phone, container, false);
    }

    public void onViewCreated(View v) {
        TextView phone = (TextView) v.findViewById(R.id.call_shopelia);
        if (!isInEditMode()) {
            phone.setText(Html.fromHtml(phone.getText().toString()));
            phone.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:01.82.09.15.44"));
                        startActivity(callIntent);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            });
        } else {
            phone.setText(phone.getText().toString());
        }
    }

    private void selfInflation() {
        mView = onCreateView(LayoutInflater.from(getContext()), this);
        onViewCreated(mView);
        removeAllViews();
        addView(mView);
    }

    public void startActivity(Intent intent) {
        getContext().startActivity(intent);
    }

    public View getView() {
        return mView;
    }

}
