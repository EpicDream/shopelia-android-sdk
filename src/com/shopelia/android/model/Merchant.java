package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.config.Config;

/**
 * Helper enum including informations about vendors
 * 
 * @author Pierre Pollastri
 */
public class Merchant implements BaseModel<Merchant> {

    public interface Api {
        String ID = "id";
        String NAME = "name";
        String LOGO = "logo";
        String URL = "url";
    }

    public static final String IDENTIFIER = Merchant.class.getName();

    public static final long INVALID_ID = -1;

    public long id = INVALID_ID;
    public String name;
    public String logo;
    public Uri uri;

    public Merchant() {

    }

    private Merchant(Parcel source) {
        id = source.readLong();
        name = source.readString();
        logo = source.readString();
        uri = source.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(logo);
        dest.writeParcelable(uri, flags);
    }

    public static final Parcelable.Creator<Merchant> CREATOR = new Creator<Merchant>() {

        @Override
        public Merchant[] newArray(int size) {
            return new Merchant[size];
        }

        @Override
        public Merchant createFromParcel(Parcel source) {
            return new Merchant(source);
        }
    };

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Api.ID, id);
        object.put(Api.NAME, name);
        object.put(Api.LOGO, logo);
        object.put(Api.URL, uri.toString());
        return object;
    }

    public static Merchant inflate(JSONObject object) throws JSONException {
        Merchant merchant = new Merchant();
        merchant.id = object.optLong(Api.ID);
        merchant.name = object.getString(Api.NAME);
        merchant.logo = object.optString(Api.LOGO);
        merchant.uri = Uri.parse(object.getString(Api.URL));
        return merchant;
    }

    public static ArrayList<Merchant> inflate(JSONArray array) {
        ArrayList<Merchant> list = new ArrayList<Merchant>(array.length());
        final int count = array.length();
        for (int index = 0; index < count; index++) {
            try {
                list.add(Merchant.inflate(array.getJSONObject(index)));
            } catch (JSONException e) {
                if (Config.ERROR_LOGS_ENABLED) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Merchant) {
            Merchant m = (Merchant) o;
            return this.id == m.id && m.id != INVALID_ID;
        }
        return super.equals(o);
    }

    @Override
    public void merge(Merchant item) {
        // TODO Auto-generated method stub

    }

}
