package com.shopelia.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.R;
import com.shopelia.android.app.CardChoregrapher.Transaction;
import com.shopelia.android.app.CardChoregrapher.Transaction.Entry;

public abstract class CardHolderActivity extends ShopeliaActivity {

    public static final int APPEND = -1;

    public static final class AddCardEvent {

        public final String name;
        public final CardFragment card;
        public final boolean allowStateLoss;
        public final int index;

        public AddCardEvent(String name, CardFragment card, boolean allowStateLoss, int index) {
            this.name = name;
            this.card = card;
            this.allowStateLoss = allowStateLoss;
            this.index = index;
        }

        public AddCardEvent(String name, CardFragment card, boolean allowStateLoss) {
            this(name, card, allowStateLoss, APPEND);
        }

        public AddCardEvent(String name, CardFragment card) {
            this(name, card, false);
        }

    }

    public static final class RemoveCardEvent {
        public final String name;
        public final CardFragment card;

        public RemoveCardEvent(CardFragment card) {
            this.name = null;
            this.card = card;
        }

        public RemoveCardEvent(String name) {
            this.name = name;
            this.card = null;
        }

    }

    private CardChoregrapher mChoregrapher = new CardChoregrapher(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_card_holder_activity);
    }

    public void addCard(CardFragment fragment, int index, boolean allowingStateLoss, String name) {
        addCard(new AddCardEvent(name, fragment, allowingStateLoss, index));
    }

    public void addCard(AddCardEvent event) {
        Transaction transaction = mChoregrapher.beginTransaction();
        transaction.addCard(event.card, event);
        transaction.commit();
    }

    public void addCardNow(CardFragment fragment, int index, boolean allowingStateLoss, String name) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.card_holder_layout, fragment, name);
        if (allowingStateLoss) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }
    }

    public void removeCard(String cardName) {
        removeCard(new RemoveCardEvent(cardName));
    }

    public void removeCard(CardFragment card) {
        removeCard(new RemoveCardEvent(card));
    }

    public void removeCard(RemoveCardEvent event) {
        Transaction transaction = mChoregrapher.beginTransaction();
        transaction.removeCard(event.card, event);
        transaction.commit();
    }

    public void removeCardNow(CardFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    public void removeCardNow(Transaction.Entry entry) {

    }

    public void addCardNow(Transaction.Entry entry) {
        AddCardEvent event = entry.getUserdata();
        addCardNow(event.card, event.index, event.allowStateLoss, event.name);
    }

    public void removeCardNow(String cardName) {
        FragmentManager fm = getSupportFragmentManager();

        Fragment f = fm.findFragmentByTag(cardName);

        if (f instanceof CardFragment) {
            removeCardNow((CardFragment) f);
        }
    }

    public CardFragment findCardByName(String name) {
        FragmentManager fm = getSupportFragmentManager();
        return (CardFragment) fm.findFragmentByTag(name);
    }

    public CardFragment getCardWithEntry(Entry entry) {
        RemoveCardEvent event = entry.getUserdata();
        return findCardByName(event.name);
    }

    public void onEventMainThread(AddCardEvent event) {
        addCard(event);
    }

    public void onEventMainThread(RemoveCardEvent event) {
        removeCard(event);
    }

}
