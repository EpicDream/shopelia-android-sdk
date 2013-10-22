package com.shopelia.android.model;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.utils.JsonUtils;
import com.shopelia.android.utils.ParcelUtils;

public class Version implements BaseModel<Version> {

	public interface Api {
		String ID = "id";
		String NAME = "name";
		String DESCRIPTION = "description";
		String IMAGE_URL = "image_url";
		String PRODUCT_PRICE = "price";
		String SHIPPING_PRICE = "price_shipping";
		String SHIPPING_EXTRAS = "shipping_info";
		String CASHFRONT_VALUE = "cashfront_value";
		String PRICE_STRIKEOUT = "price_strikeout";
		String AVAILABILITY_INFO = "availability_info";
	}

	public static final BigDecimal NO_PRICE = new BigDecimal("-1");
	public static final long FIRST_OPTION = 0;
	private final long id;

	// Prices
	private final BigDecimal productPrice;
	private final BigDecimal shippingPrice;
	private final BigDecimal cashfrontValue;
	private final BigDecimal priceStrikeOut;

	// Informations
	public final String name;
	public final String imageUrl;
	public final String description;
	public final String shippingExtra;
	public final String availabilityInfo;

	// Options
	private long optionsHashcode = FIRST_OPTION;
	private Option[] options;

	private Version(JSONObject object) throws JSONException {
		// Informations
		id = object.optLong(Api.ID);
		name = object.getString(Api.NAME);
		description = object.optString(Api.DESCRIPTION);
		shippingExtra = object.optString(Api.SHIPPING_EXTRAS);
		availabilityInfo = shippingExtra == null
				|| !shippingExtra.equalsIgnoreCase(object
						.optString(Api.AVAILABILITY_INFO)) ? object
				.optString(Api.AVAILABILITY_INFO) : null;
		imageUrl = object.optString(Api.IMAGE_URL);

		// Prices informations
		productPrice = JsonUtils.optBigDecimal(object, Api.PRODUCT_PRICE,
				NO_PRICE).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		shippingPrice = JsonUtils.optBigDecimal(object, Api.SHIPPING_PRICE,
				NO_PRICE).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		cashfrontValue = JsonUtils.optBigDecimal(object, Api.CASHFRONT_VALUE,
				BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		priceStrikeOut = JsonUtils.optBigDecimal(object, Api.PRICE_STRIKEOUT,
				NO_PRICE).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	private Version(Parcel source) {
		// Informations
		id = source.readLong();
		name = source.readString();
		description = source.readString();
		shippingExtra = source.readString();
		availabilityInfo = source.readString();
		imageUrl = source.readString();

		// Prices
		productPrice = ParcelUtils.readBigDecimal(source, NO_PRICE);
		shippingPrice = ParcelUtils.readBigDecimal(source, NO_PRICE);
		cashfrontValue = ParcelUtils.readBigDecimal(source, BigDecimal.ZERO);
		priceStrikeOut = ParcelUtils.readBigDecimal(source, NO_PRICE);

		// Options
		optionsHashcode = source.readLong();
		Parcelable[] p = source.readParcelableArray(Option.class
				.getClassLoader());
		options = new Option[p.length];
		for (int index = 0; index < p.length; index++) {
			options[index] = (Option) p[index];
		}
	}

	public void setOptions(long optionsHash, Option[] options) {
		optionsHash = Long.valueOf(optionsHash);
		this.options = options;
	}

	public Option[] getOptions() {
		return options;
	}

	public long getOptionHashcode() {
		return optionsHashcode;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public Uri getImageUri() {
		return Uri.parse(imageUrl);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Informations
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(shippingExtra);
		dest.writeString(availabilityInfo);
		dest.writeString(imageUrl);

		// Prices
		ParcelUtils.writeBigDecimal(dest, productPrice, NO_PRICE);
		ParcelUtils.writeBigDecimal(dest, shippingPrice, NO_PRICE);
		ParcelUtils.writeBigDecimal(dest, cashfrontValue, BigDecimal.ZERO);
		ParcelUtils.writeBigDecimal(dest, priceStrikeOut, NO_PRICE);

		// Options
		dest.writeLong(optionsHashcode);
		dest.writeParcelableArray(options != null ? options : new Option[0],
				flags);
	}

	public static Version inflate(JSONObject object) throws JSONException {
		return new Version(object);
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return null;
	}

	@Override
	public void merge(Version item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean isValid() {
		return productPrice != NO_PRICE && shippingPrice != NO_PRICE;
	}

	public static final Parcelable.Creator<Version> CREATOR = new Creator<Version>() {

		@Override
		public Version createFromParcel(Parcel source) {
			return new Version(source);
		}

		@Override
		public Version[] newArray(int size) {
			return new Version[size];
		}

	};

	// Price utility methods
	public BigDecimal getTotalPrice(int quantity) {
		return productPrice.multiply(BigDecimal.valueOf(quantity))
				.add(shippingPrice).subtract(cashfrontValue);
	}

	public BigDecimal getExpectedTotalPrice(int quantity) {
		return productPrice.multiply(BigDecimal.valueOf(quantity)).add(
				shippingPrice.equals(NO_PRICE) ? BigDecimal.ZERO
						: shippingPrice);
	}

	public boolean isShippingFree() {
		return shippingPrice.compareTo(BigDecimal.ZERO) <= 0;
	}

	public BigDecimal getExpectedCashfrontValue(int quantity) {
		return cashfrontValue.multiply(BigDecimal.valueOf(quantity));
	}

	/**
	 * Raw price no cashfront
	 * 
	 * @return
	 */
	public BigDecimal getPrice(int quantity) {
		return productPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public BigDecimal getStrikeoutPrice() {
		return priceStrikeOut;
	}

	public BigDecimal getShippingPrice() {
		return shippingPrice;
	}

	public boolean hasCashfront() {
		return cashfrontValue.compareTo(BigDecimal.ZERO) > 0;
	}

}
