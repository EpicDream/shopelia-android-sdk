package com.shopelia.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.shopelia.android.app.CardFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.widget.AsyncImageView;
import com.shopelia.android.widget.ViewPager;

public class ProductSummaryCardFragment extends CardFragment {

    public class OnImageAskZoomEvent {
        public final View asker;
        public final int[] position = mViewPosition;

        private OnImageAskZoomEvent(View asker) {
            this.asker = asker;
        }
    }

	public static final String TAG = "ProductSummary";

	private static final String ARGS_ALLOW_DESCRIPTION = "allow_description";

	private Product mProduct;
    private ViewPager mViewPager;
	private TextView mProductTitle;
	private TextView mProductBrand;
	private TextView mProductDescription;
	private TextView mMerchantName;
    private ImagesAdapter mImagesAdapter;
    private ValueAnimator mLeftAnimatorIn;
    private ValueAnimator mRightAnimatorIn;
    private ValueAnimator mLeftAnimatorOut;
    private ValueAnimator mRightAnimatorOut;
    private View mGoLeft;
    private View mGoRight;

	public static ProductSummaryCardFragment newInstance(
			boolean allowDescription) {
		ProductSummaryCardFragment fragment = new ProductSummaryCardFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARGS_ALLOW_DESCRIPTION, allowDescription);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shopelia_product_summary_card,
				container, false);
	}

	@Override
	public void onBindView(View view, Bundle savedInstanceState) {
		super.onBindView(view, savedInstanceState);
        mImagesAdapter = new ImagesAdapter();
        mViewPager = findViewById(R.id.product_images);
        mViewPager.setAdapter(mImagesAdapter);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
		clear(mProductTitle = findViewById(R.id.product_title));
		clear(mProductDescription = findViewById(R.id.product_description));
		clear(mMerchantName = findViewById(R.id.product_merchant_name));
		mMerchantName.setOnClickListener(mOnClickOnMerchantListener);
		clear(mProductBrand = findViewById(R.id.product_brand));
		findViewById(R.id.product_more).setOnClickListener(
				mOnClickOnMoreListener);
		if (getArguments() != null
				&& !getArguments().getBoolean(ARGS_ALLOW_DESCRIPTION, true)) {
			findViewById(R.id.product_more).setVisibility(View.GONE);
		}
        mGoLeft = findViewById(R.id.go_left);
        mGoLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() > 0) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                }
            }
        });
        mGoRight = findViewById(R.id.go_right);
        mGoRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() + 1 < mImagesAdapter.getCount()) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                }
            }
        });
        mLeftAnimatorIn = ObjectAnimator.ofFloat(mGoLeft, "alpha", 1.f).setDuration(400);
        mRightAnimatorIn = ObjectAnimator.ofFloat(mGoRight, "alpha", 1.f).setDuration(400);
        mLeftAnimatorOut = ObjectAnimator.ofFloat(mGoLeft, "alpha", 0.f).setDuration(400);
        mRightAnimatorOut = ObjectAnimator.ofFloat(mGoRight, "alpha", 0.f).setDuration(400);
        mGoLeft.setVisibility(View.GONE);
        mGoRight.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivityEventBus().registerSticky(this, Product.class);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivityEventBus().unregister(this);
	}

	// Events
	public void onEventMainThread(Product product) {
		mProduct = product;
		mProductTitle.setText(product.getCurrentVersion().name);
		mProductDescription.setText(Html.fromHtml(
                product.getCurrentVersion().description).toString());
		mMerchantName.setText(product.merchant.name);
		mProductBrand.setText(product.brand);
		mProductBrand
				.setVisibility(!TextUtils.isEmpty(product.brand) ? View.VISIBLE
						: View.GONE);
        if (needRefresh(product.getCurrentVersion().imagesUrls, mImagesAdapter.mUrls)) {
            mImagesAdapter = new ImagesAdapter();
            mImagesAdapter.update(product.getCurrentVersion().imagesUrls);
            mViewPager.setAdapter(mImagesAdapter);
            mGoLeft.setVisibility(View.GONE);
            mGoRight.setVisibility(product.getCurrentVersion().imagesUrls.length > 1 ? View.VISIBLE : View.GONE);
        }
	}

    private boolean needRefresh(String[] newUrls, String[] oldUrls) {
        if (newUrls.length == oldUrls.length) {
            for (int index = 0; index < newUrls.length; index++) {
                if (!TextUtils.equals(newUrls[index], oldUrls[index])) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

	private static TextView clear(TextView tv) {
		tv.setText(null);
		return tv;
	}

	private OnClickListener mOnClickOnMerchantListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(
					R.string.shopelia_product_description_go_merchant_title)
					.setMessage(
							R.string.shopelia_product_description_go_merchant_content);
			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent newIntent = new Intent(Intent.ACTION_VIEW,
									Uri.parse(mProduct.getMonetizedUrl()));
							startActivity(newIntent);
						}
					});
			builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.show();
		}
	};

	private OnClickListener mOnClickOnMoreListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),
					ProductDescriptionActivity.class);
			intent.putExtra(ProductDescriptionActivity.EXTRA_PRODUCT,
					getOrder().product);
			startActivity(intent);
			getTracker().track("Click On Product Description");
		}
	};

    private class ImagesAdapter extends PagerAdapter {

        private String[] mUrls;
        private LayoutInflater mInflater;

        public ImagesAdapter() {
            mUrls = new String[0];
            mInflater = LayoutInflater.from(getActivity());
        }

        public void update(String[] urls) {
            if (urls == null) {
                urls = new String[0];
            }
            mUrls = urls;
            //notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mUrls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }


        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View v = createView(mInflater, collection);
            bindView(v, position);
            collection.addView(v,0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }


        public View createView(LayoutInflater inflater, ViewGroup parent) {
            View v = inflater.inflate(R.layout.shopelia_product_image, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.image = (AsyncImageView) v.findViewById(R.id.image);
            v.setTag(holder);
            return v;
        }

        private void bindView(View v, int position) {
            ViewHolder holder = (ViewHolder) v.getTag();
            holder.image.setUrl(mUrls[position]);
            holder.image.setOnClickListener(mOnClickImageListener);
        }

        private class ViewHolder {
            AsyncImageView image;
        }

    }

    private android.support.v4.view.ViewPager.OnPageChangeListener mOnPageChangeListener = new android.support.v4.view.ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionPixelOffset) {

        }

        @Override
        public void onPageSelected(int position) {
            mGoLeft.setVisibility(View.VISIBLE);
            mGoRight.setVisibility(View.VISIBLE);
            if (position == 0 && !mLeftAnimatorOut.isRunning()) {
                mLeftAnimatorOut.start();
            } else {
                mLeftAnimatorIn.start();
            }
            if (position == mImagesAdapter.getCount() - 1) {
                mRightAnimatorOut.start();
            } else {
                mRightAnimatorIn.start();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private int[] mViewPosition = new int[2];

    private OnClickListener mOnClickImageListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
          view.getLocationInWindow(mViewPosition);
          getActivityEventBus().post(new OnImageAskZoomEvent(view));
        }
    };

}
