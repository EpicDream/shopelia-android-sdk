package com.shopelia.android.model.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.shopelia.android.AddAddressActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.BaseModel;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public enum ModelAdapterFactory {
    ADDRESS(Address.IDENTIFIER, Address.class) {
        @Override
        public BaseModelAdapter<?> getAdapter(Context context) {
            return new AddressModelAdapter(context);
        }

        @Override
        public List<? extends BaseModel> getListFromUser(User user) {
            return user.addresses;
        }

        @Override
        public Intent getIntent(Context context, BaseModel item) {
            Intent intent = new Intent(context, AddAddressActivity.class);
            if (item == null) {
                intent.putExtra(AddAddressActivity.EXTRA_MODE, AddAddressActivity.MODE_ADD);
            } else {
                Address address = (Address) item;
                intent.putExtra(AddAddressActivity.EXTRA_MODE, AddAddressActivity.MODE_EDIT);
                intent.putExtra(AddAddressActivity.EXTRA_ID, address.id);
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, address.address);
                intent.putExtra(AddAddressActivity.EXTRA_ZIPCODE, address.zipcode);
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS_EXTRAS, address.extras);
                intent.putExtra(AddAddressActivity.EXTRA_CITY, address.city);
                intent.putExtra(AddAddressActivity.EXTRA_COUNTRY, address.country);
                intent.putExtra(AddAddressActivity.EXTRA_FIRSTNAME, address.firstname);
                intent.putExtra(AddAddressActivity.EXTRA_NAME, address.name);
                intent.putExtra(AddAddressActivity.EXTRA_PHONE, address.phone);
            }
            return intent;
        }

        @Override
        public void delete(Context context, BaseModel<?> item) {
            final Address toDelete = (Address) item;
            ShopeliaRestClient.authenticate(context);
            ShopeliaRestClient.delete(Command.V1.Addresses.Address(toDelete.id), null, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {

                }

            });
            UserManager um = UserManager.get(context);
            User user = um.getUser();
            for (Address address : user.addresses) {
                if (toDelete.id == address.id) {
                    user.addresses.remove(address);
                    break;
                }
            }
            um.saveUser();
        }

    },
    PAYMENT_CARD(PaymentCard.IDENTIFIER, PaymentCard.class) {
        @Override
        public BaseModelAdapter<?> getAdapter(Context context) {
            return null;
        }

        @Override
        public Intent getIntent(Context context, BaseModel item) {
            Intent intent = new Intent(context, null);

            return null;
        }

        @Override
        public void delete(Context context, BaseModel<?> item) {
            // TODO Auto-generated method stub

        }
    };

    private String mIdentifier;
    private Class<?> mModelClass;

    private ModelAdapterFactory(String identifier, Class<?> clazz) {
        mIdentifier = identifier;
        mModelClass = clazz;
    }

    public abstract BaseModelAdapter<? extends BaseModel> getAdapter(Context context);

    public abstract Intent getIntent(Context context, BaseModel item);

    public abstract void delete(Context context, BaseModel<?> item);

    public List<? extends BaseModel> getListFromUser(User user) {
        return null;
    }

    public static ModelAdapterFactory getInstance(Class<?> clazz) {
        ModelAdapterFactory[] values = values();
        for (ModelAdapterFactory item : values) {
            if (item.mModelClass.equals(clazz)) {
                return item;
            }
        }
        return null;
    }

    public static ModelAdapterFactory getInstance(String identifier) {
        ModelAdapterFactory[] values = values();
        for (ModelAdapterFactory item : values) {
            if (item.mIdentifier.equals(identifier)) {
                return item;
            }
        }
        return null;
    }

}
