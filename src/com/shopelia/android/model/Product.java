package com.shopelia.android.model;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.ParcelUtils;
import com.shopelia.android.utils.Tax;

public class Product implements BaseModel<Product> {

	public interface Api {
		String URL = "url";
        String MONETIZED_URL = "monetized_url";
		String PRODUCT_URL = "product_url";
		String MERCHANT = "merchant";
		String VERSIONS = "versions";
		String QUANTITY = "quantity";
		String PRICE = "price";
		String PRODUCT_VERSION_ID = "product_version_id";
		String SATURN = "saturn";
		String BRAND = "brand";

	}

	public static final String IDENTIFIER = Product.class.getName();
	public static final float NO_PRICE = -1.f;

	public String url;

	public Merchant merchant;
	public Tax tax;
	public Currency currency;

	public final String brand;

	public final Versions versions;
	public long mCurrentVersionKey;
	private int mQuantity = 1;
	private boolean mFromSaturn = false;
    private String mMonetizedUrl;

	public Product(String url) {
		this.url = url;
		brand = null;
		versions = new Versions();
		ensureDefaultValues();
	}

	protected Product(JSONObject object, boolean hasVersions, int scale)
			throws JSONException {
		url = object.optString(Api.URL);
        mMonetizedUrl = object.optString(Api.MONETIZED_URL);
		if (object.has(Api.PRODUCT_URL)) {
			url = object.getString(Api.PRODUCT_URL);
		}
		brand = object.optString(Api.BRAND);
		if (object.has(Api.MERCHANT)) {
			merchant = Merchant.inflate(object.getJSONObject(Api.MERCHANT));
		}
		if (hasVersions) {
			versions = Versions.inflate(object.getJSONArray(Api.VERSIONS),
					scale);
		} else {
			versions = new Versions();
			versions.addVersion(Version.inflate(object, scale));
		}
		mFromSaturn = !object.has(Api.SATURN) || object.getInt(Api.SATURN) == 1;
		mCurrentVersionKey = versions.getFirstKey();
		ensureDefaultValues();
	}

	protected Product(Parcel source) {
		url = source.readString();
        mMonetizedUrl = source.readString();
		brand = source.readString();
		mFromSaturn = source.readByte() == 1;
		mQuantity = source.readInt();
		versions = new Versions();
		Version version = ParcelUtils.readParcelable(source,
				Version.class.getClassLoader());
		if (version != null) {
			mCurrentVersionKey = version.getOptionHashcode();
			versions.addVersion(version);
		}
		merchant = ParcelUtils.readParcelable(source,
				Merchant.class.getClassLoader());
		tax = ParcelUtils.readParcelable(source, Tax.class.getClassLoader());
		currency = ParcelUtils.readParcelable(source,
				Currency.class.getClassLoader());
	}

	public BigDecimal getTotalPrice() {
		return getCurrentVersion().getTotalPrice(mQuantity);
	}

	public BigDecimal getProductPrice() {
		return getCurrentVersion().getPrice(mQuantity);
	}

	public BigDecimal getExpectedTotalPrice() {
		return getCurrentVersion().getExpectedTotalPrice(mQuantity);
	}

	public BigDecimal getExpectedCashfrontValue() {
		return getCurrentVersion().getExpectedCashfrontValue(mQuantity);
	}

	public BigDecimal getSingleProductPrice() {
		return getCurrentVersion().getPrice(1);
	}

	public BigDecimal getStrikeoutPrice() {
		return getCurrentVersion().getStrikeoutPrice();
	}

	public BigDecimal getShippingPrice() {
		return getCurrentVersion().getShippingPrice();
	}

	public boolean hasCashfront() {
		return getCurrentVersion().hasCashfront();
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return isFromSaturn() ? toJsonFromSaturnImpl()
				: toJsonNotFromSaturnImpl();
	}

	private JSONObject toJsonFromSaturnImpl() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(Api.PRODUCT_VERSION_ID, getCurrentVersion().getId());
		json.put(Api.PRICE, getSingleProductPrice());
		json.put(Api.QUANTITY, mQuantity);
		return json;
	}

	private JSONObject toJsonNotFromSaturnImpl() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(Api.URL, url);
		json.put(Api.PRICE, getSingleProductPrice());
		json.put(Api.QUANTITY, mQuantity);
		return json;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeString(brand);
		dest.writeByte((byte) ((mFromSaturn) ? 1 : 0));
		dest.writeInt(mQuantity);
		ParcelUtils.writeParcelable(dest, getCurrentVersion(), flags);
		ParcelUtils.writeParcelable(dest, merchant, flags);
		ParcelUtils.writeParcelable(dest, tax, flags);
		ParcelUtils.writeParcelable(dest, currency, flags);
	}

	public static final Parcelable.Creator<Product> CREATOR = new Creator<Product>() {

		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}

		@Override
		public Product createFromParcel(Parcel source) {
			return new Product(source);
		}
	};

	public static Product inflate(JSONObject object) throws JSONException {
		return new Product(object, true, 1);
	}

	public static Product inflateSingleVersion(JSONObject object)
			throws JSONException {
		return new Product(object, false, 100);
	}

	public static Product inflateSingleVersion(JSONObject object, int scale)
			throws JSONException {
		return new Product(object, false, scale);
	}

	public static ArrayList<Product> inflate(JSONArray a) throws JSONException {
		final int size = a.length();
		ArrayList<Product> products = new ArrayList<Product>(size);
		for (int index = 0; index < size; index++) {
			products.add(Product.inflate(a.getJSONObject(index)));
		}
		return products;
	}

	protected void ensureDefaultValues() {
		if (currency == null) {
			currency = Currency.EUR;
		}

		if (tax == null) {
			tax = Tax.ATI;
		}

	}

	@Override
	@Deprecated
	public void merge(Product cpy) {

	}

	public void setCurrentVersion(long key) {
		mCurrentVersionKey = key;
	}

	public void setCurrentVersion(Option... options) {
		setCurrentVersion(Option.hashCode(options));
	}

	public void setCurrentVersion(int lastOptionChanged, Option... options) {
		Option[] opts = new Option[options.length];
		System.arraycopy(options, 0, opts, 0, options.length);
		if (_setCurrentVersion(0, lastOptionChanged, opts)) {
			setCurrentVersion(opts);
		}
	}

	private boolean _setCurrentVersion(int currentOption,
			int lastOptionChanged, Option[] options) {
		// Check if the version is available

		// Iterate through all option
		// call _setCurrentVersion on another option
		// If successfull stop
		// else continue

		if (!versions.hasVersion(options)) {
			Option before = options[currentOption];
			Options opts = versions.getOptions(currentOption);
			for (Option opt : opts) {
				// Compute the next available options index
				int next = getNextOptionIndexAvailable(currentOption + 1,
						lastOptionChanged);
				if (next != -1 ? _setCurrentVersion(next, lastOptionChanged,
						options) : versions.hasVersion(options)) {
					return true;
				}
				options[currentOption] = opt;
			}
			options[currentOption] = before;
			return false;
		}
		return true;
	}

	private int getNextOptionIndexAvailable(int desiredIndex, int forbidden) {
		int index = desiredIndex;
		for (; index < versions.getOptionsCount() && index == forbidden; index++)
			;
		return index < versions.getOptionsCount() && index != forbidden ? index
				: -1;
	}

	public void setQuantity(int quantity) {
		if (quantity < 1) {
			mQuantity = 1;
		} else {
			mQuantity = quantity;
		}
	}

	public int getQuantity() {
		return mQuantity;
	}

	public Version getCurrentVersion() {
		return versions.getVersion(mCurrentVersionKey);
	}

	public boolean hasVersion() {
		return versions.getVersionsCount() > 0;
	}

    public String getMonetizedUrl() {
        return TextUtils.isEmpty(mMonetizedUrl) ? url : mMonetizedUrl;
    }

	public boolean isFromSaturn() {
		return mFromSaturn;
	}

	@Override
	public long getId() {
		return url.hashCode();
	}

	@Override
	public boolean isValid() {
		return hasVersion() && getCurrentVersion().isValid()
				&& merchant != null;
	}

	public boolean isDone() {
		return false;
	}

}
