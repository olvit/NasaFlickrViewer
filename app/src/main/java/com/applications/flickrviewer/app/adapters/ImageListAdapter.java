package com.applications.flickrviewer.app.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applications.flickrviewer.app.model.FlickrImage;
import com.applications.flickrviewer.app.utils.ImageLoader;
import com.applications.flickrviewer.app.R;


public class ImageListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private FlickrAdapter flickrAdapter;
    private ImageLoader imageLoader;
    private ProgressDialog progress;

    private static class ViewHolder {
        public TextView textView;
        public ImageView imageView;
    }

    public ImageListAdapter(Activity activity, FlickrAdapter flickrAdapter) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.flickrAdapter = flickrAdapter;
        imageLoader = new ImageLoader(activity.getCacheDir());

        progress = new ProgressDialog(activity);
        progress.setTitle(activity.getResources().getString(R.string.load_text));
        progress.setMessage(activity.getResources().getString(R.string.load_message));
        progress.show();
    }

    public void setUpdateNotifier() {
        flickrAdapter.setListAdapter(this);
    }

    @Override
    public int getCount() {
        int size = flickrAdapter.getListSize();

        if (size > 0) {
            progress.dismiss();
        }

        return size;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;

        if (view == null) {
            rowView = inflater.inflate(R.layout.list_item, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.textView = (TextView) rowView.findViewById(R.id.text);
            viewHolder.imageView = (ImageView) rowView.findViewById(R.id.image);

            rowView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) rowView.getTag();

        FlickrImage flickrImage = flickrAdapter.getPhotosEntryList().get(i);

        viewHolder.textView.setText(flickrImage.getTitle());

        Bitmap imageBmp = imageLoader.loadBitmap(flickrImage, i);
        if (imageBmp == null) {
            viewHolder.imageView.setImageResource(R.drawable.thumbnail);
        } else {
            viewHolder.imageView.setImageBitmap(imageBmp);
        }

        return rowView;
    }

}
