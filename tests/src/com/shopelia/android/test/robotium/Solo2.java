package com.shopelia.android.test.robotium;

import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

public class Solo2 extends Solo {

    public Solo2(Instrumentation instrumentation) {
        super(instrumentation);
    }

    public Solo2(Instrumentation instrumentation, Activity activity) {
        super(instrumentation, activity);
    }

    public void enterTextAndNext(final EditText editText, String text) {
        final CountDownLatch barrier = new CountDownLatch(1);
        enterText(editText, text);
        editText.post(new Runnable() {

            @Override
            public void run() {
                editText.onEditorAction(EditorInfo.IME_ACTION_NEXT);
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {

        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getCurrentFocus() {
        return (T) getCurrentActivity().getCurrentFocus();
    }

}
