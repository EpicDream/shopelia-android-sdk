package com.shopelia.android.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper enum including informations about currencies
 * 
 * @author Pierre Pollastri
 */
public enum Currency implements Parcelable {
	EUR("EUR");

	private String mCurrencyCode;
	private static String sFormat;
	private static Locale sLocale;

	private Currency(String currencyCode) {
		mCurrencyCode = currencyCode;
	}

	public String format(Context context, float value) {
		BigDecimal roundfinalPrice = new BigDecimal(value).setScale(2,
				BigDecimal.ROUND_HALF_UP);
		return roundfinalPrice.toPlainString();
	}

	public String format(BigDecimal value) {
		DecimalFormat fmt = new DecimalFormat(getFormat());
		fmt.setCurrency(java.util.Currency.getInstance(mCurrencyCode));
		return fmt.format(value);
	}

	public String roundAndFormat(BigDecimal value) {
		DecimalFormat fmt = new DecimalFormat(getRoundedFormat());
		fmt.setCurrency(java.util.Currency.getInstance(mCurrencyCode));
		return fmt.format(value.round(new MathContext(2, RoundingMode.UP)));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.toString());
	}

	public static final Parcelable.Creator<Currency> CREATOR = new Creator<Currency>() {

		@Override
		public Currency[] newArray(int size) {
			return new Currency[size];
		}

		@Override
		public Currency createFromParcel(Parcel source) {
			return Currency.valueOf(source.readString());
		}
	};

	private static String getFormat() {
		if (sFormat == null || !Locale.getDefault().equals(sLocale)) {
			NumberFormat format = (DecimalFormat) DecimalFormat
					.getCurrencyInstance();
			if (format instanceof DecimalFormat) {
				String localizedPattern = ((DecimalFormat) format)
						.toLocalizedPattern();

				final boolean isPrefix = localizedPattern.indexOf('¤') == 0;
				sFormat = isPrefix ? "¤###,###,##0.00" : "###,###,##0.00¤";
			} else {
				sFormat = "¤###,###,##0.00";
			}
		}
		return sFormat;
	}

	private static String getRoundedFormat() {
		if (sFormat == null || !Locale.getDefault().equals(sLocale)) {
			NumberFormat format = (DecimalFormat) DecimalFormat
					.getCurrencyInstance();
			if (format instanceof DecimalFormat) {
				String localizedPattern = ((DecimalFormat) format)
						.toLocalizedPattern();

				final boolean isPrefix = localizedPattern.indexOf('¤') == 0;
				sFormat = isPrefix ? "¤###,###,##0" : "###,###,##0¤";
			} else {
				sFormat = "¤###,###,##0";
			}
		}
		return sFormat;
	}

}
