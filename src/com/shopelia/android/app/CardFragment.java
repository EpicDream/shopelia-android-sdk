package com.shopelia.android.app;

import android.os.Bundle;
import android.view.View;

import com.shopelia.android.app.CardChoregrapher.Transaction;

public class CardFragment extends ShopeliaFragment<Void> {

    private Transaction mTransaction;

    public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) {
        onBindView(view, savedInstanceState);
        if (mTransaction != null) {
            view.setVisibility(View.GONE);
            mTransaction.notifyViewCreated();
            onCardShouldAppear(mTransaction);
            mTransaction = null;
        } else {
            onCardShouldBeVisible();
        }
    }

    public void attachTransaction(Transaction transaction) {
        mTransaction = transaction;
    }

    public void onCardShouldAppear(final Transaction transaction) {
        transaction.endTransaction();
    }

    public void onBindView(View view, Bundle savedInstanceState) {

    }

    public void onCardShouldBeVisible() {
        getView().setVisibility(View.VISIBLE);
    }

    public void onCardShouldDisappear(final Transaction transaction) {
        transaction.endTransaction();
    }

}
