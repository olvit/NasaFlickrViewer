package com.applications.flickrviewer.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import com.applications.flickrviewer.app.model.FlickrImage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final String LOG_TAG = "ImageLoader";
    private static final int NUMBER_OF_THREADS = 4;
    private static final int BMP_LRU_CACHE_SIZE = 50 * 1024 * 1024;
    private static final int IMAGE_DOWNLOAD_DELAY_MS = 100;

    private static int lastListItemIndex = 0;

    private ExecutorService executorService;
    private ImageCache imageCache;
    private LruCache<Integer, Bitmap> bmpLruCache;

    public ImageLoader(File cacheDir) {
        imageCache = new ImageCache(cacheDir);
        bmpLruCache = new LruCache<Integer, Bitmap>(BMP_LRU_CACHE_SIZE);

        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    }

    private void getBitmap(String imageId, String imageUrlStr, int index) {
        Bitmap imageBitmap;

        File cachedImage = imageCache.getCachedImage(imageId);

        if (cachedImage.exists()) {
            imageBitmap = BitmapFactory.decodeFile(cachedImage.getAbsolutePath());
        } else {
            try {
                Thread.sleep(IMAGE_DOWNLOAD_DELAY_MS);
                if (Math.abs(lastListItemIndex - index) >= NUMBER_OF_THREADS) {
                    return;
                }
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "Unable to sleep download thread for item #" + index +": " + e.getMessage());
            }

            (new NetworkConnector()).downloadToFile(imageUrlStr, cachedImage);

            imageBitmap = BitmapFactory.decodeFile(cachedImage.getAbsolutePath());
        }

        if (imageBitmap != null) {
            bmpLruCache.put(index, imageBitmap);
        } else {
            Log.w(LOG_TAG, "Wrong Image bitmap for list index: " + index);
            imageCache.removeFromCache(imageId);
        }
    }

    private class ImageLoaderTask implements Runnable {

        private FlickrImage flickrImage;
        private int index;

        private ImageLoaderTask(FlickrImage flickrImage, int index) {
            this.flickrImage = flickrImage;
            this.index = index;
        }

        @Override
        public void run() {
            getBitmap(flickrImage.getId(), flickrImage.getUrl(), index);
        }

    }

    public Bitmap loadBitmap(FlickrImage flickrImage, int index) {

        lastListItemIndex = index;

        Bitmap imageBmp = bmpLruCache.get(index);
        if (imageBmp == null) {
            ImageLoaderTask loaderTask = new ImageLoaderTask(flickrImage, index);
            executorService.execute(loaderTask);
        }

        return imageBmp;
    }
}
