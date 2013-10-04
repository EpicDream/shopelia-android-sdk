package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.view.animation.AnimationHelper;
import com.shopelia.android.view.animation.AnimationHelper.OnAnimationCompleteListener;

public class ErrorCardFragment extends CardFragment {

    public static class DismissEvent {

    }

    private static final String ARGS_MESSAGE = "args:message";

    public static ErrorCardFragment newInstance(int messageId) {
        ErrorCardFragment fragment = new ErrorCardFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_MESSAGE, messageId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ErrorCardFragment newInstance(CharSequence message) {
        ErrorCardFragment fragment = new ErrorCardFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message.toString());
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
    }

    @Override
    public void onCardShouldBeVisible() {
        super.onCardShouldBeVisible();
        // AnimationHelper.playVerticalLandIn(getView(), 200, 800, null);
        getActivityEventBus().register(this, DismissEvent.class);
        findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Should finish activity
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

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    public void onEvent(DismissEvent event) {
        AnimationHelper.playVerticalSlideOut(getView(), -200, 200, new OnAnimationCompleteListener() {

            @Override
            public void onAnimationComplete() {
                // getActivityEventBus().post(new
                // BaseActivity.DetachFragmentEvent(ErrorCardFragment.this));
            }
        });
    }

}
