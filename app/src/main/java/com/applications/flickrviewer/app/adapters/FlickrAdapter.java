package com.applications.flickrviewer.app.adapters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.BaseAdapter;

import com.applications.flickrviewer.app.model.FlickrImage;
import com.applications.flickrviewer.app.utils.NetworkConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FlickrAdapter {

    private static final int DEFAULT_LIST_SIZE = 300;
    private static final int FLICKR_REST_RESPONCE_PREFIX = 14;
    private static final String LOG_TAG = "FlickrAdapter";

    private final String photosetIdUrl;
    private final String photoListIdUrl;
    private final String photoIdUrl;

    private String flickrApiKey;
    private String flickrUserId;
    private NetworkConnector networkConnector;
    private BaseAdapter adapter;
    private Handler handler;

    private static ArrayList<FlickrImage> photosList;

    public FlickrAdapter(String apiKey, String userId) {
        StringBuilder urlStringBuilder = new StringBuilder();

        this.flickrApiKey = apiKey;
        this.flickrUserId = userId;

        urlStringBuilder.
                append("http://www.flickr.com/services/rest/?method=flickr.photosets.getList&format=json&api_key=").
                append(flickrApiKey).
                append("&user_id=").
                append(flickrUserId);
        photosetIdUrl = urlStringBuilder.toString();

        urlStringBuilder.setLength(0);
        urlStringBuilder.
                append("http://www.flickr.com/services/rest/?method=flickr.photosets.getPhotos&format=json&api_key=").
                append(flickrApiKey).
                append("&photoset_id=");
        photoListIdUrl = urlStringBuilder.toString();

        urlStringBuilder.setLength(0);
        urlStringBuilder.
                append("http://www.flickr.com/services/rest/?method=flickr.photos.getInfo&format=json&api_key=").
                append(flickrApiKey).
                append("&photo_id=");
        photoIdUrl = urlStringBuilder.toString();

        networkConnector = new NetworkConnector();
        photosList = new ArrayList<FlickrImage>(DEFAULT_LIST_SIZE);
        handler = new Handler(Looper.getMainLooper());
    }

    public void setListAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    private class PhotoInfo {
        private String url;
        private String title;

        public PhotoInfo(String url, String title) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {

            return url;
        }
    }

    private PhotoInfo loadPhotoInfo(String photoId) {
        String title = null;
        String secretId;
        String serverId;
        String farmId;
        StringBuilder urlStringBuilder = null;

        String serverResponce = networkConnector.GetRequest(photoIdUrl + photoId);
        if (serverResponce == null) {
            return null;
        }

        serverResponce = serverResponce.substring(FLICKR_REST_RESPONCE_PREFIX);

        try {
            JSONObject jsonObj = (new JSONObject(serverResponce)).getJSONObject("photo");

            secretId = jsonObj.getString("secret");
            serverId = jsonObj.getString("server");
            farmId   = jsonObj.getString("farm");

            urlStringBuilder = new StringBuilder().
                    append("http://farm").append(farmId).
                    append(".staticflickr.com/").append(serverId).append("/").
                    append(photoId).append("_").append(secretId).append(".jpg");

            title = jsonObj.getJSONObject("title").getString("_content");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error on parse Photo Info JSON: " + e.getMessage());
        }

        return new PhotoInfo(urlStringBuilder.toString(), title);
    }

    private class UpdateNotifierTask implements Runnable {

        BaseAdapter adapter;

        public UpdateNotifierTask(BaseAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadPhotosList(String photosetId) {
        String serverResponce = networkConnector.GetRequest(photoListIdUrl + photosetId);
        if (serverResponce == null) {
            return;
        }

        serverResponce = serverResponce.substring(FLICKR_REST_RESPONCE_PREFIX);

        try {
            JSONObject jsonObj = new JSONObject(serverResponce);
            JSONArray photosJsonArray = jsonObj.getJSONObject("photoset").getJSONArray("photo");

            for (int i = 0; i < photosJsonArray.length(); ++i) {
                String photoId = photosJsonArray.getJSONObject(i).getString("id");
                PhotoInfo photoInfo = loadPhotoInfo(photoId);
                synchronized (this) {
                    if (photoInfo != null) {
                        photosList.add(new FlickrImage(photoId, photoInfo.getUrl(), photoInfo.getTitle()));
                    }
                }

                handler.post(new UpdateNotifierTask(adapter));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error on parse Photo list JSON: " + e.getMessage());
        }
    }

    private void loadPhotosetsList() {
        int photosetListSize;

        String serverResponce = networkConnector.GetRequest(photosetIdUrl);
        if (serverResponce == null) {
            return;
        }

        serverResponce = serverResponce.substring(FLICKR_REST_RESPONCE_PREFIX);

        try {
            JSONObject jsonObj = new JSONObject(serverResponce);
            photosetListSize = jsonObj.getJSONObject("photosets").getInt("total");
            JSONArray photosetJsonArray = jsonObj.getJSONObject("photosets").getJSONArray("photoset");

            for (int i = 0; i < photosetListSize; ++i) {
                loadPhotosList(photosetJsonArray.getJSONObject(i).getString("id"));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error on parse PhotoSet list JSON: " + e.getMessage());
        }
    }

    public void loadFlickrImages() {
        loadPhotosetsList();
    }

    public synchronized int getListSize() {
        return photosList.size();
    }

    public synchronized ArrayList<FlickrImage> getPhotosEntryList() {
        return photosList;
    }

}
