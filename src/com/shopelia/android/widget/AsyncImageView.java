package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ListView;

import com.shopelia.android.R;
import com.shopelia.android.image.ImageLoader;
import com.shopelia.android.image.ImageRequest;

/**
 * <p>
 * A {@link AsyncImageView} is a network-aware ImageView. It may display images
 * from the web according to a URL. {@link AsyncImageView} takes care of loading
 * asynchronously images on the Internet. It also caches images in an
 * application-wide cache to prevent loading images several times.
 * </p>
 * <p>
 * Clients may listen the {@link OnAsyncImageViewLoadListener} to be notified of
 * the current image loading state.
 * </p>
 * <p>
 * {@link AsyncImageView} may be extremely useful in ListView's row. To prevent
 * your {@link AsyncImageView} from downloading while scrolling or flinging it
 * is a good idea to pause it using {@link ImageLoader#setPaused(boolean)}
 * method. Alternatively, you can use a {@link ListView} which will take care of
 * pausing/unpausing the {@link ImageLoader} when necessary.
 * </p>
 * 
 * @author Cyril Mottier
 */
public class AsyncImageView extends ImageView {

    private static final int DEFAULT_CROSS_FADING_DURATION = 300;
    private static final Drawable EMPTY_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    private static final long DELAY_FOR_CROSS = 800L;

    private long mStartLoading = 0;

    /**
     * Clients may listen to {@link AsyncImageView} changes using a
     * {@link OnAsyncImageViewLoadListener}.
     * 
     * @author Cyril Mottier
     */
    public static interface OnAsyncImageViewLoadListener {

        /**
         * Called when the image started to load
         * 
         * @param imageView The AsyncImageView that started loading
         */
        void onLoadingStarted(AsyncImageView imageView);

        /**
         * Called when the image ended to load that is when the image has been
         * downloaded and is ready to be displayed on screen
         * 
         * @param imageView The AsyncImageView that ended loading
         */
        void onLoadingEnded(AsyncImageView imageView, Bitmap image);

        /**
         * Called when the image loading failed
         * 
         * @param imageView The AsyncImageView that failed to load
         * @param exception The Exception that occurred
         */
        void onLoadingFailed(AsyncImageView imageView, Exception exception);
    }

    public static final int DEFAULT_MAX_RETRY = 10;

    private Drawable mDefaultDrawable;

    private String mUrl;
    private ImageRequest mRequest;
    private boolean mIsLoading;

    private boolean mForceLoad = false;

    private Bitmap mBitmap;
    private OnAsyncImageViewLoadListener mOnAsyncImageViewLoadListener;
    private BitmapFactory.Options mOptions;

    private boolean mIsCrossFadingEnabled;
    private int mCrossFadingDuration;
    private boolean mIsFixedSize;

    private boolean mBlockLayout;

    private int mMaxRetry = DEFAULT_MAX_RETRY;
    private int mAttempts = 0;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initAsyncImageView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView, defStyle, 0);

        Drawable d = a.getDrawable(R.styleable.AsyncImageView_shopelia_defaultSrc);
        if (d != null) {
            setDefaultImageDrawable(d);
        }

        final int inDensity = a.getInt(R.styleable.AsyncImageView_shopelia_inDensity, -1);
        if (inDensity != -1) {
            setInDensity(inDensity);
        }

        setCrossFadingEnabled(a.getBoolean(R.styleable.AsyncImageView_shopelia_crossFadingEnabled, mIsCrossFadingEnabled));
        setCrossFadingDuration(a.getInt(R.styleable.AsyncImageView_shopelia_crossFadingDuration, mCrossFadingDuration));
        setUrl(a.getString(R.styleable.AsyncImageView_shopelia_url));
        mIsFixedSize = a.getBoolean(R.styleable.AsyncImageView_shopelia_isFixedSize, false);

        a.recycle();
    }

    @Override
    public void setImageURI(Uri uri) {
        setUrl(uri.toString());
    }

    private void initAsyncImageView() {
        mIsCrossFadingEnabled = true;
        mCrossFadingDuration = DEFAULT_CROSS_FADING_DURATION;
    }

    public void forceDownload() {
        mForceLoad = true;
        if (mRequest != null) {
            mRequest.reload(getContext());
        }
    }

    public void setForceDownload(boolean forceDownload) {
        mForceLoad = forceDownload;
    }

    public boolean isForcingDownload() {
        return mForceLoad;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mIsFixedSize) {
            mBlockLayout = true;
        }
        super.setImageDrawable(drawable);
        mBlockLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayout) {
            super.requestLayout();
        }
    }

    public boolean isCrossFadingEnabled() {
        return mIsCrossFadingEnabled;
    }

    public void setCrossFadingEnabled(boolean isCrossFadingEnabled) {
        mIsCrossFadingEnabled = isCrossFadingEnabled;
    }

    public int getCrossFadingDuration() {
        return mCrossFadingDuration;
    }

    public void setCrossFadingDuration(int crossFadingDuration) {
        mCrossFadingDuration = crossFadingDuration;
    }

    private boolean canCrossFade() {
        return mIsCrossFadingEnabled && mCrossFadingDuration > 0;
    }

    public boolean isFixedSize() {
        return mIsFixedSize;
    }

    /**
     * Return true if this AsyncImageView is currently loading an image.
     * 
     * @return true if this AsyncImageView is currently loading an image.
     *         Otherwise it returns false.
     */
    public boolean isLoading() {
        return mRequest != null;
    }

    /**
     * Return true if the displayed image has been correctly loaded.
     * 
     * @return true if this AsyncImageView succeed to load the image at the
     *         given url.
     */
    public boolean isLoaded() {
        return mRequest == null && mBitmap != null;
    }

    /**
     * Bitmap accessor
     * 
     * @return the image's bitmap
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (mIsFixedSize) {
            mBlockLayout = true;
        }
        mBitmap = bitmap;
        super.setImageBitmap(bitmap);
        mBlockLayout = false;
    }

    /**
     * Helper to {@link #setOptions(Options)} that simply sets the inDensity for
     * loaded image.
     * 
     * @param inDensity
     * @see AsyncImageView#setOptions(Options)
     */
    public void setInDensity(int inDensity) {
        if (mOptions == null) {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = true;
            mOptions.inScaled = true;
            mOptions.inTargetDensity = getContext().getResources().getDisplayMetrics().densityDpi;
        }

        mOptions.inDensity = inDensity;
    }

    /**
     * Assign an Options object to this {@link AsyncImageView}. Those options
     * are used internally by the {@link AsyncImageView} when decoding the
     * image. This may be used to prevent the default behavior that loads all
     * images as mdpi density.
     * 
     * @param options
     */
    public void setOptions(BitmapFactory.Options options) {
        mOptions = options;
    }

    /**
     * Reload the image pointed by the given URL. You may want to force
     * reloading by setting the force parameter to true.
     * 
     * @param force if true the AsyncImageView won't look into the
     *            application-wide cache.
     */
    public boolean reload() {
        if (mRequest == null && mUrl != null) {
            mBitmap = null;
            mRequest = ImageRequest.obtain(mUrl, mImageRequestCallback);
            mIsLoading = true;
            final boolean isSynchronous = mRequest.load(getContext());
            mIsLoading = false;
            if (!isSynchronous) {
                setDefaultImage();
            }
            return isSynchronous;
        }
        return true;
    }

    /**
     * Force the loading to be stopped.
     */
    public void stopLoading() {
        if (mRequest != null) {
            mRequest.cancel();
            mRequest = null;
        }
    }

    /**
     * Register a callback to be invoked when an event occured for this
     * AsyncImageView.
     * 
     * @param listener The listener that will be notified
     */
    public void setOnAsyncImageViewLoadListener(OnAsyncImageViewLoadListener listener) {
        mOnAsyncImageViewLoadListener = listener;
    }

    /**
     * Set the url of the image that will be used as the content of this
     * AsyncImageView. The given may be null in order to display the default
     * image. Please note the url may be a local url. For instance, you can
     * asynchronously load images from the disk memory is the url scheme is
     * <code>file://</code>
     * 
     * @param url The url of the image to set. Pass null to force the
     *            AsyncImageView to display the default image
     */
    public boolean setUrl(String url) {
        mAttempts = 0;
        // Check the url has changed
        if (url != null && url.equals(mUrl) && (mBitmap != null || (mRequest != null && !mRequest.isCancelled()))) {
            return true;
        }

        stopLoading();
        mUrl = url;

        // Setting the url to an empty string force the displayed image to the
        // default image
        if (TextUtils.isEmpty(url)) {
            mBitmap = null;
            setDefaultImage();
        } else {
            return reload();
        }
        return true;
    }

    /**
     * Set the default bitmap as the content of this AsyncImageView
     * 
     * @param bitmap The bitmap to set
     */
    public void setDefaultImageBitmap(Bitmap bitmap) {
        setDefaultImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    /**
     * Set the default drawable as the content of this AsyncImageView
     * 
     * @param drawable The drawable to set
     */
    public void setDefaultImageDrawable(Drawable drawable) {
        mDefaultDrawable = drawable;
        setDefaultImage();
    }

    /**
     * Set the default resource as the content of this AsyncImageView
     * 
     * @param resId The resource identifier to set
     */
    public void setDefaultImageResource(int resId) {
        setDefaultImageDrawable(getResources().getDrawable(resId));
    }

    private void setDefaultImage() {
        if (mBitmap == null) {
            setImageDrawable(mDefaultDrawable);
        }
    }

    static class SavedState extends BaseSavedState {
        String url;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            url = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(url);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.url = mUrl;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setUrl(ss.url);
    }

    private ImageRequest.Callback mImageRequestCallback = new ImageRequest.Callback() {

        public void onImageRequestStarted(ImageRequest request) {
            if (mOnAsyncImageViewLoadListener != null) {
                mOnAsyncImageViewLoadListener.onLoadingStarted(AsyncImageView.this);
            }
            mStartLoading = System.currentTimeMillis();
        }

        public void onImageRequestFailed(ImageRequest request, Exception exception) {
            mRequest = null;
            if (++mAttempts < mMaxRetry) {
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        stopLoading();
                        forceDownload();
                        reload();
                    }
                }, 500);
            }
            if (mOnAsyncImageViewLoadListener != null) {
                mOnAsyncImageViewLoadListener.onLoadingFailed(AsyncImageView.this, exception);
            }
        }

        public void onImageRequestEnded(ImageRequest request, Bitmap image) {
            mRequest = null;
            mBitmap = image;
            if (mStartLoading + DELAY_FOR_CROSS > System.currentTimeMillis()) {
                setImageBitmap(image);
                return;
            }
            if (canCrossFade() && !mIsLoading) {
                final TransitionDrawable d = new TransitionDrawable(new Drawable[] {
                        mDefaultDrawable == null ? EMPTY_DRAWABLE : new KeepRatioDrawable(mDefaultDrawable),
                        new KeepRatioDrawable(new BitmapDrawable(getResources(), image))
                });
                setImageDrawable(d);
                d.setCrossFadeEnabled(true);
                d.startTransition(mCrossFadingDuration);
            } else {
                setImageBitmap(image);
            }

            if (mOnAsyncImageViewLoadListener != null) {
                mOnAsyncImageViewLoadListener.onLoadingEnded(AsyncImageView.this, image);
            }
        }

        public void onImageRequestCancelled(ImageRequest request) {
            mRequest = null;
            if (mOnAsyncImageViewLoadListener != null) {
                mOnAsyncImageViewLoadListener.onLoadingFailed(AsyncImageView.this, null);
            }
        }

        @Override
        public boolean onSlowConnection() {
            return mForceLoad;
        }

        @Override
        public void onPause() {

        }

        @Override
        public void onResume() {

        }
    };

    /**
     * A very basic Drawable that keeps the ratio of the underlying Drawable
     * whenever possible. This compensate the framework's Drawables as none of
     * them respect the ratio when being resized.
     * 
     * @author Cyril Mottier
     */
    private static class KeepRatioDrawable extends Drawable {

        private Drawable mDrawable;

        public KeepRatioDrawable(Drawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public void draw(Canvas canvas) {
            mDrawable.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            mDrawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mDrawable.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return mDrawable.getOpacity();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            final int width = mDrawable.getIntrinsicWidth();
            final int height = mDrawable.getIntrinsicHeight();
            if (width > 0 && height > 0) {
                final int containerWidth = bounds.width();
                final int containerHeight = bounds.height();

                final float scale = Math.min((float) containerWidth / (float) width, (float) containerHeight / (float) height);
                final int dx = (int) ((containerWidth - width * scale) * 0.5f + 0.5f);
                final int dy = (int) ((containerHeight - height * scale) * 0.5f + 0.5f);

                mDrawable.setBounds(dx, dy, (int) (width * scale + dx), (int) (height * scale + dy));
            } else {
                mDrawable.setBounds(bounds);
            }
        }

    }

}
