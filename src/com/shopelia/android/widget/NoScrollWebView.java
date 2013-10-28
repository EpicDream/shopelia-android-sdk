package com.shopelia.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class NoScrollWebView extends WebView {

	public NoScrollWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoScrollWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollWebView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// requestDisallowInterceptTouchEvent(true);
		return super.onTouchEvent(event);
	}

}
