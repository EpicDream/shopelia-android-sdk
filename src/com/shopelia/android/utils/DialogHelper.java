package com.shopelia.android.utils;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;

import com.shopelia.android.R;
import com.shopelia.android.app.ShopeliaAlertDialog;

public final class DialogHelper {

    private DialogHelper() {

    }

    public static ShopeliaAlertDialog.Builder buildLogoutDialog(Context context, OnClickListener onPositiveClickListener,
            OnClickListener onNegativeClickListener) {
        ShopeliaAlertDialog.Builder builder = new ShopeliaAlertDialog.Builder(context);
        //@formatter:off
        builder
            .setTitle(R.string.shopelia_dialog_title)
            .setMessage(R.string.shopelia_logout_are_you_sure)
            .setPositiveButton(R.string.shopelia_logout_positive, onPositiveClickListener)
            .setNegativeButton(R.string.shopelia_logout_cancel, onNegativeClickListener);
        //@formatter:on
        return builder;
    }
}
