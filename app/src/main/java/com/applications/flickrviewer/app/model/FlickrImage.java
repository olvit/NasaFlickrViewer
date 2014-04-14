package com.applications.flickrviewer.app.model;


public class FlickrImage {

    private String id;
    private String url;
    private String title;

    public FlickrImage(String id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {

        return title;
    }

    public String getUrl() {

        return url;
    }

    public String getId() {

        return id;
    }
}
