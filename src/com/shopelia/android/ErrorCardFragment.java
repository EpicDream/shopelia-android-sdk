package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.view.animation.AnimationHelper;
import com.shopelia.android.view.animation.AnimationHelper.OnAnimationCompleteListener;

public class ErrorCardFragment extends CardFragment {

    public static class DismissEvent {

    }

    public static class OnErrorButtonClickEvent {

    }

    private static final OnErrorButtonClickEvent EVENT_ON_ERROR_BUTTON_CLICK = new OnErrorButtonClickEvent();

    private static final String ARGS_MESSAGE = "args:message";
    private static final String ARGS_BUTTON = "args:button";

    public static ErrorCardFragment newInstance(CharSequence message, CharSequence button) {
        ErrorCardFragment fragment = new ErrorCardFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message.toString());
        args.putString(ARGS_BUTTON, button.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_error_card_fragment, container, false);
    }

    @Override
    public void onBindView(View view, Bundle savedInstanceState) {
        super.onBindView(view, savedInstanceState);
        setMessage(getMessage());
        setButtonMessage(getButtonMessage());
        getActivityEventBus().register(this, DismissEvent.class);
    }

    @Override
    public void onCardShouldBeVisible() {
        super.onCardShouldBeVisible();
        AnimationHelper.playVerticalSlideIn(getView(), 200, 800, null);
        findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivityEventBus().post(EVENT_ON_ERROR_BUTTON_CLICK);
            }
        });
    }

    public void setMessage(CharSequence message) {
        TextView view = findViewById(R.id.error_message);
        view.setText(message);
    }

    public CharSequence getMessage() {
        Object message = getArguments().get(ARGS_MESSAGE);
        if (message instanceof CharSequence) {
            return (CharSequence) message;
        } else if (message instanceof Integer) {
            int messageId = ((Integer) message).intValue();
            CharSequence sequence = getText(messageId);
            return sequence;
        }
        return null;
    }

    public void setButtonMessage(CharSequence message) {
        TextView view = findViewById(R.id.back_button_message);
        view.setText(message);
    }

    public CharSequence getButtonMessage() {
        Object message = getArguments().get(ARGS_BUTTON);
        if (message instanceof CharSequence) {
            return (CharSequence) message;
        } else if (message instanceof Integer) {
            int messageId = ((Integer) message).intValue();
            CharSequence sequence = getText(messageId);
            return sequence;
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    public void onEvent(DismissEvent event) {
        AnimationHelper.playVerticalSlideOut(getView(), -200, 200, new OnAnimationCompleteListener() {

            @Override
            public void onAnimationComplete() {
                getActivityEventBus().post(new ShopeliaActivity.RemoveFragmentEvent(ErrorCardFragment.this));
            }
        });
    }

}
