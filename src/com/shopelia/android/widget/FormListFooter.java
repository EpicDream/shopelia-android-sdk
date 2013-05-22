package com.shopelia.android.widget;

import android.content.Context;
import android.text.Html;
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
            mFooterText = (FontableTextView) mView.findViewById(R.id.cgu);
            mFooterText.setText(Html.fromHtml(mContext.getString(R.string.shopelia_form_main_footer_text)));
        }
        return mView;
    }

    public void setView(View view) {
        mView = view;
        mFooterText = (FontableTextView) mView.findViewById(R.id.cgu);
        mFooterText.setText(Html.fromHtml(mContext.getString(R.string.shopelia_form_main_footer_text)));
    }

}
