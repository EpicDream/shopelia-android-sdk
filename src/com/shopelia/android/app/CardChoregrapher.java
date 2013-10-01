package com.shopelia.android.app;

import java.util.ArrayList;
import com.shopelia.android.app.CardChoregrapher.Transaction.Entry;

public class CardChoregrapher {

    private ArrayList<CardFragment> mCards = new ArrayList<CardFragment>();
    private Transaction mTransaction;
    private CardHolderActivity mOwner;

    public CardChoregrapher(CardHolderActivity owner) {
        mOwner = owner;
    }

    public Transaction beginTransaction() {
        return new Transaction();
    }

    private void push(Transaction transaction) {
        if (mTransaction == null) {
            mTransaction = transaction;
            invalidate();
        } else {
            Transaction cursor = mTransaction;
            while (cursor.next != null) {
                cursor = cursor.next;
            }
            cursor.next = transaction;
        }
    }

    private void pop(Transaction transaction) {
        if (transaction == mTransaction) {
            mTransaction = mTransaction.next;
            invalidate();
        }
    }

    /**
     * Should be only used when mTransaction has changed
     */
    private void invalidate() {
        if (mTransaction != null) {
            mTransaction.process();
        }
    }

    public class Transaction {

        private static final int ACTION_ADD = 0;
        private static final int ACTION_DELETE = 1;

        public class Entry {
            private CardFragment card;
            private Object mUserdata;
            private int action;

            public Entry(CardFragment card, int action, Object userData) {
                this.card = card;
                this.action = action;
                this.mUserdata = userData;
            }

            public <T> T getUserdata() {
                return (T) mUserdata;
            }

        }

        private Transaction next;
        private ArrayList<Entry> mEntries = new ArrayList<CardChoregrapher.Transaction.Entry>();

        public void endTransaction() {
            if (mEntries.get(0).action == ACTION_DELETE) {
                Entry entry = mEntries.get(0);
                mCards.remove(entry.card);
                if (entry.card != null) {
                    mOwner.removeCardNow(entry);
                }
            }
            pop(this);
        }

        public void addCard(CardFragment fragment, Object userdata) {
            fragment.attachTransaction(this);
            mEntries.add(new Entry(fragment, ACTION_ADD, userdata));
        }

        public void removeCard(CardFragment fragment, Object userdata) {
            mEntries.add(new Entry(fragment, ACTION_DELETE, userdata));
        }

        public void commit() {
            push(this);
        }

        public void notifyViewCreated() {
            // TODO Should place the Card on the view group
        }

        private void process() {
            for (Entry entry : mEntries) {
                process(entry);
            }
        }

        private void process(Entry entry) {
            switch (entry.action) {
                case ACTION_ADD:
                    mCards.add(entry.card);
                    mOwner.addCardNow(entry);
                    break;
                case ACTION_DELETE:
                    if (entry.card == null) {
                        entry.card = mOwner.getCardWithEntry(entry);
                    }
                    if (entry.card != null) {
                        entry.card.onCardShouldDisappear(this);
                    } else {
                        endTransaction();
                    }
                    break;
            }
        }
    }

}
