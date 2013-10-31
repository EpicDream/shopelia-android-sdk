package com.shopelia.android.image;

import android.content.Context;
import android.graphics.Bitmap;

import com.shopelia.android.image.ImageLoader.WorkerHandler;

/**
 * An {@link ImageRequest} may be used to request an image from the network. The
 * process of requesting for an image is done in three steps:
 * <ul>
 * <li>Instantiate a new {@link ImageRequest}</li>
 * <li>Call {@link #load(Context)} to start loading the image</li>
 * <li>Listen to loading state changes using a {@link Callback}</li>
 * </ul>
 * 
 * @author Cyril Mottier
 */
public class ImageRequest {

	/**
	 * @author Cyril Mottier
	 */
	public interface Callback {

		/**
		 * Callback to be invoked when the request processing started.
		 * 
		 * @param request
		 *            The ImageRequest that started
		 */
		void onImageRequestStarted(ImageRequest request);

		/**
		 * Callback to be invoked when the request processing failed.
		 * 
		 * @param request
		 *            ImageRequest that failed
		 * @param exception
		 *            The Exception that occurs
		 */
		void onImageRequestFailed(ImageRequest request, Exception exception);

		/**
		 * Callback to be invoked when the request processing ended.
		 * 
		 * @param request
		 *            ImageRequest that ended
		 * @param image
		 *            The resulting Bitmap
		 */
		void onImageRequestEnded(ImageRequest request, Bitmap image);

		/**
		 * Callback to be invoked when the request processing has been
		 * cancelled.
		 * 
		 * @param request
		 *            ImageRequest that has been cancelled
		 */
		void onImageRequestCancelled(ImageRequest request);

		/**
		 * Callback to be invoked when the {@link ImageLoader} detects a slow
		 * connection.
		 * 
		 * @return true if you want to force the download of the image. A wiser
		 *         behavior would be returning false.
		 */
		boolean onSlowConnection();

		/**
		 * Callback to be invoked when the request is being paused because of
		 * bad connection
		 */
		void onPause();

		/**
		 * Callback to be invoked when the request was paused and is about to be
		 * send to a worker thread for download.
		 */
		void onResume();

	}

	public static class CallbackAdapter implements Callback {

		@Override
		public void onImageRequestStarted(ImageRequest request) {
		}

		@Override
		public void onImageRequestFailed(ImageRequest request,
				Exception exception) {
		}

		@Override
		public void onImageRequestEnded(ImageRequest request, Bitmap image) {
		}

		@Override
		public void onImageRequestCancelled(ImageRequest request) {
		}

		@Override
		public boolean onSlowConnection() {
			return false;
		}

		@Override
		public void onPause() {

		}

		@Override
		public void onResume() {

		}

	}

	public static final int CACHE_POLICY_NONE = 0;
	public static final int CACHE_POLICY_MEMORY = 1 << 0;
	public static final int CACHE_POLICY_DISK = 1 << 1;
	public static final int CACHE_POLICY_NETWORK = 1 << 2;
	public static final int CACHE_POLICY_LOCAL = CACHE_POLICY_MEMORY
			| CACHE_POLICY_DISK;
	public static final int CACHE_POLICY_DEFAULT = CACHE_POLICY_MEMORY
			| CACHE_POLICY_DISK | CACHE_POLICY_NETWORK;

	private static final int MAX_POOL_SIZE = 10;

	private static ImageLoader sImageLoader;

	private static final Object sPoolSync = new Object();
	private static ImageRequest sPool;
	private static int sPoolSize = 0;

	/**
	 * The URL of the image to load
	 */
	public String url;

	/**
	 * The cache policy defines where to look for AND cache the information.
	 */
	public int cachePolicy = CACHE_POLICY_DEFAULT;

	/**
	 * The callback that may be used by clients to listen to the loading process
	 * and be notified of the changes that happened
	 */
	public ImageRequest.Callback callback;

	public ImagePostTreatment postTreatment;

	public boolean downloaded = false;

	/* package */boolean isCancelled;
	/* package */int retryCount;
	/* package */WorkerHandler handler;
	/* package */Object result;
	/* package */ImageRequest next;
	/* package */boolean isPaused;

	/**
	 * Constructor (but the preferred way to get a {@link ImageRequest} is to
	 * call {@link ImageRequest#obtain()}).
	 */
	public ImageRequest() {
	}

	/**
	 * Return a new {@link ImageRequest} instance from the global pool. Allows
	 * us to avoid allocating new objects in many cases.
	 */
	public static ImageRequest obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				ImageRequest request = sPool;
				sPool = request.next;
				request.next = null;
				sPoolSize--;
				return request;
			}
		}
		return new ImageRequest();
	}

	/**
	 * Same as {@link #obtain()} but also sets the url as well as the callback
	 * to the ImageRequest
	 * 
	 * @param url
	 *            The url
	 * @param callback
	 * @return
	 */
	public static ImageRequest obtain(String url, ImageRequest.Callback callback) {
		final ImageRequest request = obtain();
		request.url = url;
		request.cachePolicy = CACHE_POLICY_DEFAULT;
		request.callback = callback;
		return request;
	}

	/**
	 * Return an {@link ImageRequest} instance to the global pool. You MUST NOT
	 * touch the Message after calling this function -- it has effectively been
	 * freed.
	 */
	public void recycle() {
		clearForRecycle();

		synchronized (sPoolSync) {
			if (sPoolSize < MAX_POOL_SIZE) {
				next = sPool;
				sPool = this;
				sPoolSize++;
			}
		}
	}

	public boolean load(Context context) {
		if (sImageLoader == null) {
			sImageLoader = ImageLoader.get(context);
		}
		return sImageLoader.loadRequest(context, this);
	}

	public void reload(Context context) {
		if (sImageLoader == null) {
			sImageLoader = ImageLoader.get(context);
		}
		sImageLoader.dispatch();
	}

	public void cancel() {
		if (!isCancelled) {
			isCancelled = true;
			sImageLoader.cancelRequest(this);
			if (callback != null) {
				callback.onImageRequestCancelled(this);
			}
			// Now the request has been cancelled, let's make sure we keep no
			// reference to any possible Contexts by null-ing the callback (no
			// useful anymore)
			callback = null;
		}
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	private void clearForRecycle() {
		url = null;
		cachePolicy = 0;
		callback = null;

		isCancelled = false;
		retryCount = 0;
		handler = null;
		result = null;
		next = null;
	}

	@Override
	public String toString() {
		return "ImageRequest {url: " + url + "}";
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

}
