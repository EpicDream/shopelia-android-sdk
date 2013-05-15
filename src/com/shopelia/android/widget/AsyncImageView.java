package com.shopelia.android.widget;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.shopelia.android.utils.DigestUtils;

public class AsyncImageView extends ImageView {

    private static final String CACHE_DIR = "shopelia/cache/";
    private static final long MIN_TIME_FOR_TRANSITION = 500L;
    private DownloadImage mDownloadTask;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    @Override
    public void setImageURI(Uri uri) {
        if (uri.getScheme().contains("http") && mDownloadTask == null) {
            mDownloadTask = new DownloadImage();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
            } else {
                mDownloadTask.execute(uri);
            }
        } else {
            super.setImageURI(uri);
        }
    }

    private class DownloadImage extends AsyncTask<Uri, Void, Drawable> {
        private File mCacheDir;
        private long mStartTime;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCacheDir = new File(getContext().getFilesDir(), CACHE_DIR);
            if (mCacheDir.exists() && !mCacheDir.isDirectory()) {
                mCacheDir.delete();
            }
            if (!mCacheDir.exists()) {
                mCacheDir.mkdirs();
            }
            mStartTime = System.currentTimeMillis();
        }

        @Override
        protected Drawable doInBackground(Uri... args) {
            if (args.length == 1) {
                String url = args[0].toString();
                String filename = DigestUtils.SHA1(url, "UTF-8");
                if (filename == null) {
                    return null;
                }
                File imageFile = new File(mCacheDir, filename);
                if (!imageFile.exists() && !download(url, imageFile)) {
                    return null;
                }
                try {
                    return new BitmapDrawable(getResources(), BitmapFactory.decodeStream(new FileInputStream(imageFile)));
                } catch (FileNotFoundException e) {

                }
            }
            return null;
        }

        protected boolean download(String urlString, File outFile) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                is = new BufferedInputStream(connection.getInputStream());
                fos = new FileOutputStream(outFile);

                byte[] buffer = new byte[4096];
                int read = 0;
                while ((read = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                }
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {

                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if (result != null) {
                Drawable drawable = result;
                if (getDrawable() != null && System.currentTimeMillis() - mStartTime > MIN_TIME_FOR_TRANSITION) {
                    drawable = new TransitionDrawable(new Drawable[] {
                            new KeepRatioDrawable(getDrawable()), new KeepRatioDrawable(drawable)
                    });
                    ((TransitionDrawable) drawable).setCrossFadeEnabled(true);
                    ((TransitionDrawable) drawable).startTransition(getResources().getInteger(android.R.integer.config_longAnimTime));
                }
                setImageDrawable(drawable);
            }
            mDownloadTask = null;
        }

    }

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

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable instanceof BitmapDrawable) {
            super.setImageDrawable(new KeepRatioDrawable(getDrawable()));
        }
    }
}
