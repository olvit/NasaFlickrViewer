package com.applications.flickrviewer.app.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class ImageCache {

    private static final String LOG_TAG = "ImageCache";
    private static final String CACHE_DIR = "FlickrViewerCache";
    private static final int MAX_CACHE_SIZE = 100;

    private int maxSize;
    private File cacheDir;

    public ImageCache(File cacheDir, int maxCacheSize) {
        this.cacheDir = cacheDir;
        this.maxSize = maxCacheSize;

        if (cacheDir == null) {
            Log.w(LOG_TAG, "Wrong Image cache directory, try to create new one.");

            cacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), CACHE_DIR);

            if (!cacheDir.exists()) {
                if (!cacheDir.mkdirs()) {
                    Log.w(LOG_TAG, "Unable to create Image cache directory: " + cacheDir.getAbsolutePath());
                }
            }
        }
    }

    public ImageCache(File cacheDir) {
        this(cacheDir, MAX_CACHE_SIZE);
    }

    public int getMaxCacheSize() {
        return maxSize;
    }

    public boolean isImageCached(String imageId) {
        return (new File(cacheDir.getAbsolutePath(), imageId)).exists();
    }

    public File getCachedImage(String imageId) {
        return new File(cacheDir, imageId);
    }

    public void removeFromCache(String imageId) {
        File cacheFile = new File(cacheDir, imageId);

        if (cacheFile.exists()) {
            if (!cacheFile.delete()) {
                Log.w(LOG_TAG, "Unable to delete Image cache fiel: " + cacheFile.getAbsolutePath());
            }
        }
    }

    public void clearCache() {
        File[] cachedFiles = cacheDir.listFiles();

        if(cachedFiles == null) {
            return;
        }

        for(File file:cachedFiles) {
            if (!file.delete()) {
                Log.w(LOG_TAG, "Unable to clear Image cache directory: " + cacheDir.getAbsolutePath());
            }
        }
    }

}
