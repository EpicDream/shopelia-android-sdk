package com.shopelia.android.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.shopelia.android.R;

public class FormListHeader {

    private Context mContext;
    private View mView;

    public FormListHeader(Context context) {
        mContext = context;
    }

    public View getView() {
        if (mView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mView = inflater.inflate(R.layout.shopelia_form_list_header, null);
        }
        return mView;
    }

}
