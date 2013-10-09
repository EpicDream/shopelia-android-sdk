package com.shopelia.android.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.shopelia.android.R;

public class FormListFooter {

    private final Context mContext;
    private View mView;
    private FontableTextView mFooterText;

    public FormListFooter(Context context) {
        mContext = context;
    }

    public View getView() {
        if (mView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mView = inflater.inflate(R.layout.shopelia_form_list_footer, null);
        }
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

}
