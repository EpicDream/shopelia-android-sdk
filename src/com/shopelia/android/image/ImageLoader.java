package com.shopelia.android.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.shopelia.android.app.ContextCompat;
import com.shopelia.android.config.Config;
import com.shopelia.android.graphics.BitmapCompat;
import com.shopelia.android.os.DiskSpace;
import com.shopelia.android.utils.IOUtils;
import com.shopelia.android.utils.TimeUnits;

/**
 * This class is not thread-safe and MUST BE used from the UI Thread
 * 
 * @author Cyril Mottier
 */
// TODO Cyril: Start & Quit worker Threads independently. There is no need to
// start 3 workers for a single image for instance.
public class ImageLoader {

    private static final String LOG_TAG = "ImageLoader";

    private static final String ASSETS_PREFIX = "icons://";

    private static final String CACHE_DIR = "shopelia/images";

    private static final double DISK_CACHE_PERCENTAGE = 0.01f;
    private static final long DISK_INTERNAL_MINIMUM_CACHE_SIZE = 10 * 1024 * 1024;
    private static final long DISK_EXTERNAL_MINIMUM_CACHE_SIZE = 30 * 1024 * 1024;
    //
    private static final long DISK_ENTITY_TIME_TO_LIVE = 7 * 24 * 60 * 60 * 1000;

    private static final int WORKER_POOL_COUNT = 3;
    private static final int DEFAULT_RETRY_COUNT = 0;
    private static final long KEEP_ALIVE_DURATION = 30 * 1000;
    private static final long FILE_EXPIRATION_DELAY = 3 * TimeUnits.MONTHS;

    // Messages that will be used by the WorkerHandlers (associated with a
    // worker thread).
    private static final int MESSAGE_LOAD = 100;

    // Messages that will be used by the Handler associated with the UI Thread.
    private static final int MESSAGE_ON_START = 200;
    private static final int MESSAGE_ON_END = 201;
    private static final int MESSAGE_ON_FAIL = 202;
    private static final int MESSAGE_ON_QUIT = 203;

    private static class WorkerRecord {
        WorkerHandler handler;
        HandlerThread worker;
    }

    private static ImageLoader sInstance;

    private final LruCache<String, Bitmap> mHardMemoryCache;
    private final Handler mHandler;
    private final LinkedList<ImageRequest> mRequestsQueue;

    /**
     * Define the maximum size (in bytes) of a Bitmap that can be cached in the
     * in-memory cache. Images bigger than this size will never be cached.
     */
    private final int mMaximumMemoryCachedImageSize;

    /**
     * Define the maximum size (in bytes) of a Bitmap that can be cached in the
     * on-disk cache. Images bigger than this size will never be cached.
     */
    private final int mMaximumDiskCachedImageSize;

    private boolean mIsPaused;

    private Cache mCache;
    private ArrayList<WorkerRecord> mWorkers;

    private AssetManager mAssetManager;

    private ImageLoader(Context context) {

        final Context appContext = context.getApplicationContext();

        mHandler = new MainHandler(Looper.getMainLooper());

        mRequestsQueue = new LinkedList<ImageRequest>();

        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mHardMemoryCache = new LruCache<String, Bitmap>((int) (activityManager.getMemoryClass() / 8f * 1024f * 1024f)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return BitmapCompat.getByteCount(value);
            };
        };

        File cacheDir;
        long cacheSize;
        final DiskSpace diskSpace = new DiskSpace(context);
        final File externalCacheDir = ContextCompat.getExternalCacheDir(context);
        if (externalCacheDir != null) {
            cacheDir = new File(externalCacheDir, CACHE_DIR);
            cacheSize = Math.max(DISK_EXTERNAL_MINIMUM_CACHE_SIZE,
                    (long) (diskSpace.getExternalStorageTotalSpace() * DISK_CACHE_PERCENTAGE));
        } else {
            cacheDir = new File(context.getCacheDir(), CACHE_DIR);
            cacheSize = Math.max(DISK_INTERNAL_MINIMUM_CACHE_SIZE,
                    (long) (diskSpace.getInternalStorageTotalSpace() * DISK_CACHE_PERCENTAGE));
        }
        mCache = new Cache(cacheDir, FILE_EXPIRATION_DELAY, cacheSize);

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        // Half-size of the screen in bytes, in ARGB_8888 format
        mMaximumMemoryCachedImageSize = 4 * metrics.widthPixels * metrics.heightPixels / 2;
        mMaximumDiskCachedImageSize = 5 * mMaximumMemoryCachedImageSize;

        mAssetManager = appContext.getAssets();
    }

    public static ImageLoader get(Context context) {
        if (sInstance == null) {
            sInstance = new ImageLoader(context);
        }
        return sInstance;
    }

    public void cancelRequest(ImageRequest request) {
        mRequestsQueue.remove(request);
    }

    public void clearUrlFromCache(String url) {
        mCache.delete(url);
        mHardMemoryCache.remove(url);
    }

    public boolean loadRequest(Context context, ImageRequest request) {
        // First ensure the URL is valid
        final String url = request.url;
        if (TextUtils.isEmpty(url)) {
            final ImageRequest.Callback callback = request.callback;
            if (callback != null) {
                callback.onImageRequestStarted(request);
                callback.onImageRequestFailed(request, new IllegalArgumentException("The url cannot be null or empty"));
            }
            return true;
        }

        Bitmap bitmap = null;

        if ((request.cachePolicy & ImageRequest.CACHE_POLICY_MEMORY) != 0 && bitmap == null) {
            // Look into the synchronous cache
            bitmap = mHardMemoryCache.get(url);
        }
        if (url.startsWith(ASSETS_PREFIX)) {
            if (bitmap == null) {

                // Is the image present in the assets?
                InputStream is = null;
                try {
                    is = mAssetManager.open(url.replace(ASSETS_PREFIX, "icons/"));
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                } finally {
                    IOUtils.closeQuietly(is);
                }

                if (bitmap != null) {
                    mHardMemoryCache.put(url, bitmap);
                }
            }

            // Let's return it synchronously.
            final ImageRequest.Callback callback = request.callback;
            if (callback != null) {
                callback.onImageRequestStarted(request);
                if (bitmap != null) {
                    callback.onImageRequestEnded(request, bitmap);
                } else {
                    callback.onImageRequestFailed(request, new IllegalArgumentException("Unable to decode: " + url));
                }
            }

            return true;
        }

        if (bitmap != null) {
            // The Bitmap is already available
            // Let's return it synchronously.
            final ImageRequest.Callback callback = request.callback;
            if (callback != null) {
                callback.onImageRequestStarted(request);
                callback.onImageRequestEnded(request, bitmap);
            }
            return true;
        }

        // Ensure the workers won't be quit
        mHandler.removeMessages(MESSAGE_ON_QUIT);

        // The image is not in the memory cache. We know have to try loading
        // from the disk cache or the network in an asynchronous manner.
        final WorkerHandler workerHandler = mIsPaused ? null : getWorkerHandler();

        if (workerHandler == null) {
            // There is no available WorkerHandler. We have to enqueue this
            // request in order to process it in the future
            mRequestsQueue.remove(request);
            mRequestsQueue.addFirst(request);
        } else {
            // There is at least a worker available. Let's flag it as not
            // available and forward the ImageRequest to it.
            sendRequestToWorker(request, workerHandler);
        }

        return false;
    }

    public void setPaused(boolean paused) {
        if (mIsPaused != paused) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "setPaused: " + paused);
            }
            mIsPaused = paused;
            if (paused) {
                postQuitWorkersIfNecessary();
            } else {
                WorkerHandler handler;
                while ((handler = getWorkerHandler()) != null && !mRequestsQueue.isEmpty()) {
                    sendRequestToWorker(mRequestsQueue.removeLast(), handler);
                }
            }
        }
    }

    public void dispatch() {
        WorkerHandler handler;
        while (!isRequestQueueEmpty() && (handler = getWorkerHandler()) != null) {
            sendRequestToWorker(removeLast(), handler);
        }
    }

    /**
     * Flushes the in-memory cache releasing as much memory as possible. This
     * method should generally not be used. It may only be required when
     * entering a feature requiring a LOT of memory.
     */
    public void flush() {
        mHardMemoryCache.evictAll();
    }

    /**
     * Find an available worker Thread. In case a worker Thread is available, it
     * returns a WorkerHandler that can be used to interact with it. When no
     * worker Thread is available, null is returned. This method takes care of
     * starting the worker Threads when necessary
     * 
     * @return An Handler that can be used to interact with a worker Thread or
     *         null if no worker Thread is currently available.
     */
    private WorkerHandler getWorkerHandler() {
        if (mWorkers == null) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "Starting all workers");
            }
            mWorkers = new ArrayList<WorkerRecord>(WORKER_POOL_COUNT);
            for (int i = 0; i < WORKER_POOL_COUNT; i++) {
                final WorkerRecord record = new WorkerRecord();
                record.worker = new HandlerThread(LOG_TAG + " worker#" + i, Process.THREAD_PRIORITY_BACKGROUND);
                record.worker.start();
                record.handler = new WorkerHandler(record.worker.getLooper());
                mWorkers.add(record);
            }
        }

        for (int i = 0; i < WORKER_POOL_COUNT; i++) {
            final WorkerRecord record = mWorkers.get(i);
            if (record.handler.isAvailable) {
                return record.handler;
            }
        }

        return null;
    }

    private void sendRequestToWorker(ImageRequest request, WorkerHandler workerHandler) {
        // There is at least a worker available. Let's flag it as not
        // available and forward the ImageRequest to it.
        workerHandler.isAvailable = false;

        request.handler = workerHandler;

        if (request.isPaused && request.callback != null) {
            request.callback.onResume();
        }

        request.isPaused = false;
        Message msg = Message.obtain(workerHandler);
        msg.what = MESSAGE_LOAD;
        msg.obj = request;
        msg.sendToTarget();
    }

    private boolean canQuitWorkers() {
        if (mRequestsQueue.isEmpty() && mWorkers != null) {
            boolean canQuitWorkers = true;
            for (WorkerRecord record : mWorkers) {
                canQuitWorkers &= record.handler.isAvailable;
            }
            return canQuitWorkers;
        }

        return false;
    }

    private void quitWorkers() {
        if (mWorkers != null) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "Quitting all workers");
            }
            for (WorkerRecord record : mWorkers) {
                record.handler.quit();
                record.worker.quit();
            }
            mWorkers = null;
        }
    }

    private void postQuitWorkersIfNecessary() {
        if (canQuitWorkers()) {
            mHandler.removeMessages(MESSAGE_ON_QUIT);
            mHandler.sendEmptyMessageDelayed(MESSAGE_ON_QUIT, KEEP_ALIVE_DURATION);
        }
    }

    public String dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("ImageLoader [\n");
        builder.append("  Workers: {\n");
        if (mWorkers != null) {
            for (WorkerRecord record : mWorkers) {
                builder.append("    ");
                builder.append(record.worker);
                builder.append("(available=");
                builder.append(record.handler.isAvailable);
                builder.append(")\n");
            }
        }
        builder.append("  }");
        builder.append("  Queue: {\n");
        for (ImageRequest request : mRequestsQueue) {
            builder.append("    ");
            builder.append(request.url);
            builder.append("\n");
        }
        builder.append("  }");

        return builder.toString();
    }

    public boolean isRequestQueueEmpty() {
        Iterator<ImageRequest> it = mRequestsQueue.listIterator();
        while (it.hasNext()) {
            ImageRequest request = it.next();
            if (request.callback != null && request.callback.onSlowConnection()) {
                return false;
            }
        }
        return true;
    }

    private ImageRequest removeLast() {
        ListIterator<ImageRequest> it = mRequestsQueue.listIterator(mRequestsQueue.size());
        while (it.hasPrevious()) {
            ImageRequest request = it.previous();
            if (request.callback != null && request.callback.onSlowConnection()) {
                it.remove();
                return request;
            }
        }
        return null;
    }

    /* package */class WorkerHandler extends Handler {

        public boolean isAvailable;

        public WorkerHandler(Looper looper) {
            super(looper);
            isAvailable = true;
        }

        @Override
        public void handleMessage(Message msg) {

            final ImageRequest request = (ImageRequest) msg.obj;
            switch (msg.what) {
                case MESSAGE_LOAD:
                    // Do the work of loading the image
                    // First look into the disk cache
                    // Secondly use the network
                    // Post everything back to the main Thread
                    final Message startReply = Message.obtain(mHandler);
                    startReply.what = MESSAGE_ON_START;
                    startReply.obj = request;
                    startReply.sendToTarget();

                    Bitmap diskBitmap = null;
                    Bitmap networkBitmap = null;
                    Exception exception = null;
                    // 1: Look into the disk cache
                    if ((request.cachePolicy & ImageRequest.CACHE_POLICY_DISK) != 0) {
                        try {
                            if (mCache.exists(request.url)) {
                                long creationDate = mCache.getCreationDate(request.url);
                                if (System.currentTimeMillis() - creationDate < DISK_ENTITY_TIME_TO_LIVE) {
                                    diskBitmap = BitmapFactory.decodeStream(new FileInputStream(mCache.load(request.url)));
                                }
                            }
                        } catch (OutOfMemoryError error) {
                            ImageLoader.this.flush();
                        } catch (Exception e) {
                            if (Config.ERROR_LOGS_ENABLED) {
                                Log.e(LOG_TAG, "Unable to retrieve the on-disk cached image", e);
                            }
                        } finally {

                        }
                    }

                    // 2: Hit the network in order to retrieve the Bitmap
                    if (diskBitmap == null && (request.cachePolicy & ImageRequest.CACHE_POLICY_NETWORK) != 0) {
                        boolean tempSuccess = false;

                        request.retryCount = 0;
                        File file = mCache.create(request.url);
                        while (request.retryCount <= DEFAULT_RETRY_COUNT) {
                            InputStream iStream = null;
                            OutputStream oStream = null;
                            try {
                                iStream = new BufferedInputStream(new URL(request.url).openStream());
                                oStream = new BufferedOutputStream(new FileOutputStream(file));
                                IOUtils.copy(iStream, oStream);
                                tempSuccess = true;
                            } catch (Exception e) {
                                exception = e;
                            } finally {
                                request.retryCount++;
                                IOUtils.closeQuietly(iStream);
                                IOUtils.closeQuietly(oStream);
                            }

                            InputStream input = null;
                            try {
                                input = tempSuccess ? new FlushedInputStream(new FileInputStream(file)) : new FlushedInputStream(new URL(
                                        request.url).openStream());
                                networkBitmap = BitmapFactory.decodeStream(input, null, null);
                                if (networkBitmap != null) {
                                    break;
                                }
                            } catch (Exception e) {
                                exception = e;
                            } catch (Throwable t) {
                                if (t instanceof Error) {
                                    throw (Error) t;
                                } else {
                                    throw new RuntimeException(t);
                                }
                            } finally {
                                IOUtils.closeQuietly(input);
                            }
                        }

                        if (networkBitmap == null && exception == null) {
                            // Skia returned a null bitmap ... that's usually
                            // because the given url wasn't pointing to a valid
                            // image or the image was too big to be decoded
                            exception = new Exception("Skia image decoding failed");
                        }
                    }

                    // 4: Notify the UI Thread the loading process has been
                    // completed
                    final Bitmap bitmap = networkBitmap != null ? networkBitmap : diskBitmap;
                    final boolean fail = bitmap == null;
                    final Message endReply = Message.obtain(mHandler);
                    endReply.what = fail ? MESSAGE_ON_FAIL : MESSAGE_ON_END;
                    request.result = fail ? exception : bitmap;
                    endReply.obj = request;
                    endReply.sendToTarget();
                    break;
            }
        }

        private void quit() {

        }
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            // Shortcut for the MESSAGE_ON_QUIT message
            if (msg.what == MESSAGE_ON_QUIT) {
                // Every workers are going to sleep so we do not need to listen
                // connection anymore
                quitWorkers();
                return;
            }

            final ImageRequest request = (ImageRequest) msg.obj;
            final ImageRequest.Callback callback = request.callback;
            boolean finalizeRequest = false;

            switch (msg.what) {
                case MESSAGE_ON_START:
                    if (callback != null) {
                        callback.onImageRequestStarted(request);
                    }
                    break;

                case MESSAGE_ON_END:
                    final Bitmap bitmap = (Bitmap) request.result;
                    // Store the Bitmap in the in-memory cache
                    if ((request.cachePolicy & ImageRequest.CACHE_POLICY_MEMORY) != 0) {
                        if (BitmapCompat.getByteCount(bitmap) < mMaximumMemoryCachedImageSize) {
                            mHardMemoryCache.put(request.url, bitmap);
                        }
                    }
                    // Notify the callback
                    if (callback != null && !request.isCancelled) {
                        callback.onImageRequestEnded(request, (Bitmap) request.result);
                    }
                    finalizeRequest = true;
                    break;

                case MESSAGE_ON_FAIL:
                    if (callback != null && !request.isCancelled) {
                        callback.onImageRequestFailed(request, (Exception) request.result);
                    }
                    finalizeRequest = true;
                    break;
            }

            if (finalizeRequest) {
                // First we need to dequeue or flag the worker thread as
                // "available" so that it can be use for latter use.
                if (!mIsPaused && !isRequestQueueEmpty()) {
                    sendRequestToWorker(removeLast(), request.handler);
                } else {
                    request.handler.isAvailable = true;
                }

                // Post a message to quit the worker threads automatically
                // after a delay of non-use.
                postQuitWorkersIfNecessary();

                // Recycle the ImageRequest if necessary
                request.recycle();
            }
        }
    }

    // See http://code.google.com/p/android/issues/detail?id=6066
    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int byteCount = read();
                    if (byteCount < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

}
