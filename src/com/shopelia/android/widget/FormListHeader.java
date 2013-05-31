package com.shopelia.android.widget;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.R;
import com.shopelia.android.model.Merchant;

public class FormListHeader {

    private Activity mContext;
    private View mView;

    public FormListHeader(Activity context) {
        mContext = context;
    }

    public View getView() {
        if (mView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mView = inflater.inflate(R.layout.shopelia_form_list_header, null);
            TextView introductionText = (TextView) mView.findViewById(R.id.introduction_text);
            String vendor = "";
            if (mContext.getIntent().hasExtra(PrepareOrderActivity.EXTRA_MERCHANT)) {
                Merchant v = mContext.getIntent().getParcelableExtra(PrepareOrderActivity.EXTRA_MERCHANT);
                vendor = v.name;
            }
            introductionText.setText(Html.fromHtml(mContext.getString(R.string.shopelia_form_main_header_text, vendor)));
        }
        return mView;
    }

    public void setView(View view) {
        mView = view;
        TextView introductionText = (TextView) mView.findViewById(R.id.introduction_text);
        String vendor = "";
        if (mContext.getIntent().hasExtra(PrepareOrderActivity.EXTRA_MERCHANT)) {
            Merchant v = mContext.getIntent().getParcelableExtra(PrepareOrderActivity.EXTRA_MERCHANT);
            vendor = v.name;
        }
        introductionText.setText(Html.fromHtml(mContext.getString(R.string.shopelia_form_main_header_text, vendor)));
    }

}
