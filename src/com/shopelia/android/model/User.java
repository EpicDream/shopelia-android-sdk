package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements JsonData {

    public interface Api {
        String USER = "user";
        String AUTH_TOKEN = "auth_token";

        String ID = "id";
        String EMAIL = "email";
        String PASSWORD = "password";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String ADDRESSES_ATTRIBUTES = "addresses_attributes";

        String PHONE = "phone";

    }

    public long id;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;

    public User() {

    }

    public User(JSONObject json) throws JSONException {
        this();
        id = json.getLong(Api.ID);
        email = json.getString(Api.EMAIL);
        firstName = json.getString(Api.FIRST_NAME);
        lastName = json.getString(Api.LAST_NAME);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.EMAIL, email);
        json.put(Api.FIRST_NAME, firstName);
        json.put(Api.LAST_NAME, lastName);
        return json;
    }

}
