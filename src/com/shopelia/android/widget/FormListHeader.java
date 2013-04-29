package com.shopelia.android.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
            TextView introductionText = (TextView) mView.findViewById(R.id.introduction_text);
            introductionText.setText(Html.fromHtml(mContext.getString(R.string.shopelia_form_main_header_text, "d'un marchand")));
        }
        return mView;
    }

}
