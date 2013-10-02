package com.shopelia.android.model;

import java.io.StringWriter;

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

    private Option(Parcel source) {
        text = source.readString();
        src = source.readString();
    }

    public boolean isText() {
        return text != null;
    }

    public boolean isImage() {
        return src != null;
    }

    public String getValue() {
        return isText() ? text : src;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(src);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject();
    }

    @Override
    public String toString() {
        return getValue();
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
        if (options == null || options.length == 0) {
            return 0;
        }
        StringWriter writer = new StringWriter();
        for (Option option : options) {
            writer.append(option.getValue());
            writer.append('&');
        }
        return writer.toString().hashCode();
    }

    public static final Creator<Option> CREATOR = new Creator<Option>() {

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }

        @Override
        public Option createFromParcel(Parcel source) {
            return new Option(source);
        }
    };

}
