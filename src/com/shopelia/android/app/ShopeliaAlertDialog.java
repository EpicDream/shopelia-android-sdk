package com.shopelia.android.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.shopelia.android.R;
import com.shopelia.android.widget.FontableTextView;

public class ShopeliaAlertDialog extends ShopeliaDialog {

    private String mPositiveButton;
    private String mNegativeButton;
    private String mNeutralButton;
    private OnClickListener mOnPositiveClickListener = null;
    private OnClickListener mOnNeutralClickListener = null;
    private OnClickListener mOnNegativeClickListener = null;
    private View mView;
    private String mMessage;
    private String mTitle;

    private int mButtonCount = 0;

    public ShopeliaAlertDialog(Context context) {
        super(context);
        setContentView(R.layout.shopelia_alert_dialog);
        setPositiveButton(null, null);
        setNegativeButton(null, null);
        setNeutralButton(null, null);
        setMessage(null);
    }

    public void setPositiveButton(String positiveButton, OnClickListener l) {
        mPositiveButton = positiveButton;
        mOnPositiveClickListener = l;
        FontableTextView view = (FontableTextView) findViewById(R.id.positive);
        int before = view.getVisibility();
        view.setVisibility(positiveButton != null ? View.VISIBLE : View.GONE);
        view.setText(positiveButton);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnPositiveClickListener != null) {
                    mOnPositiveClickListener.onClick(ShopeliaAlertDialog.this, DialogInterface.BUTTON_POSITIVE);
                }
            }
        });
        if (before != view.getVisibility()) {
            mButtonCount += view.getVisibility() != View.GONE ? 1 : -1;
        }
        refreshButtonLayout();
        LinearLayout.LayoutParams p = (LayoutParams) view.getLayoutParams();
        p.weight = 1;
        findViewById(R.id.positive_divider).setVisibility(view.getVisibility());
        view.requestLayout();
    }

    public void setNegativeButton(String negativeButton, OnClickListener l) {
        mNegativeButton = negativeButton;
        mOnNegativeClickListener = l;
        FontableTextView view = (FontableTextView) findViewById(R.id.negative);
        int before = view.getVisibility();
        view.setVisibility(negativeButton != null ? View.VISIBLE : View.GONE);
        view.setText(negativeButton);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancel();
                if (mOnNegativeClickListener != null) {
                    mOnNegativeClickListener.onClick(ShopeliaAlertDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
            }
        });
        if (before != view.getVisibility()) {
            mButtonCount += view.getVisibility() != View.GONE ? 1 : -1;
        }
        refreshButtonLayout();
        LinearLayout.LayoutParams p = (LayoutParams) view.getLayoutParams();
        p.weight = 1;
        view.requestLayout();
    }

    public void setNeutralButton(String neutralButton, OnClickListener l) {
        mNeutralButton = neutralButton;
        mOnNegativeClickListener = l;
        FontableTextView view = (FontableTextView) findViewById(R.id.neutral);
        int before = view.getVisibility();
        view.setVisibility(neutralButton != null ? View.VISIBLE : View.GONE);
        view.setText(neutralButton);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnNeutralClickListener != null) {
                    mOnNeutralClickListener.onClick(ShopeliaAlertDialog.this, DialogInterface.BUTTON_NEUTRAL);
                }
            }
        });
        if (before != view.getVisibility()) {
            mButtonCount += view.getVisibility() != View.GONE ? 1 : -1;
        }
        refreshButtonLayout();
        LinearLayout.LayoutParams p = (LayoutParams) view.getLayoutParams();
        p.weight = 1;
        findViewById(R.id.neutral_divider).setVisibility(view.getVisibility());
        view.requestLayout();
    }

    protected void refreshButtonLayout() {
        LinearLayout l = (LinearLayout) findViewById(R.id.buttons_container);
        l.setWeightSum(mButtonCount);
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
        FrameLayout layout = (FrameLayout) findViewById(R.id.custom_view_container);
        layout.removeAllViews();
        if (view != null) {
            layout.addView(view);
        }
        layout.setVisibility(view != null ? View.VISIBLE : View.GONE);
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
        FontableTextView view = (FontableTextView) findViewById(R.id.message);
        view.setText(message);
        view.setVisibility(message != null ? View.VISIBLE : View.GONE);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public static class Builder {

        private Context mContext;
        private ShopeliaAlertDialog mDialog;

        public Builder(Context context) {
            mContext = context;
            mDialog = new ShopeliaAlertDialog(mContext);
        }

        public Context getContext() {
            return mContext;
        }

        public Builder setView(View view) {
            mDialog.setView(view);
            return this;
        }

        public Builder setTitle(int resId) {
            return setTitle(getContext().getString(resId));
        }

        public Builder setTitle(CharSequence title) {
            mDialog.setTitle(title.toString());
            return this;
        }

        public Builder setMessage(int resId) {
            return setMessage(getContext().getString(resId));
        }

        public Builder setMessage(CharSequence message) {
            mDialog.setMessage(message.toString());
            return this;
        }

        public Builder setPositiveButton(int resId, OnClickListener l) {
            return setPositiveButton(getContext().getString(resId), l);
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener l) {
            mDialog.setPositiveButton(text.toString(), l);
            return this;
        }

        public Builder setNegativeButton(int resId, OnClickListener l) {
            return setNegativeButton(getContext().getString(resId), l);
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener l) {
            mDialog.setNegativeButton(text.toString(), l);
            return this;
        }

        public Builder setNeutralButton(int resId, OnClickListener l) {
            return setNeutralButton(getContext().getString(resId), l);
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener l) {
            mDialog.setNeutralButton(text.toString(), l);
            return this;
        }

        public ShopeliaAlertDialog create() {
            return mDialog;
        }

        public ShopeliaAlertDialog show() {
            ShopeliaAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }

    }

}
