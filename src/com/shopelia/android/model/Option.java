package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

public class Option implements BaseModel<Option> {

    public interface Api {
        String OPTION = "option";
        String TEXT = "text";
        String SRC = "src";
    }

    public final String text;
    public final String src;

    private Option(JSONObject object) throws JSONException {
        this.text = object.optString(Api.TEXT);
        this.src = object.optString(Api.SRC);
        if (!isText() && !isImage()) {
            throw new JSONException("Should hold either src or text");
        }
    }

    public boolean isText() {
        return text != null;
    }

    public boolean isImage() {
        return src != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject();
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void merge(Option item) {

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Option) {
            Option option = (Option) o;
            return isText() ? text.equals(option.text) : src.equals(option.src);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return isText() ? text.hashCode() : src.hashCode();
    }

    public static Option[] inflateFromVersion(JSONObject object) throws JSONException {
        final int size = getOptionsCount(object);
        Option[] options = new Option[size];
        for (int index = 0; index < size; index++) {
            options[index] = new Option(object.getJSONObject(Api.OPTION + (index + 1)));
        }
        return options;
    }

    public static int getOptionsCount(JSONObject object) {
        int index;
        for (index = 1; object.has(Api.OPTION + index); index++)
            ;
        return index - 1;
    }

    public static long hashCode(Option... options) {
        return 0;
    }

}
