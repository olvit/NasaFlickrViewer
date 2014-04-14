package com.applications.flickrviewer.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.applications.flickrviewer.app.R;
import com.applications.flickrviewer.app.adapters.FlickrAdapter;
import com.applications.flickrviewer.app.adapters.ImageListAdapter;


public class FlickrActivity extends Activity {

    private static final String FLICKR_API_KEY = "4da5ad1e79bc011e6f459dab2438d2ff";
    private static final String FLICKR_NASA_UID = "24662369@N07";

    private ImageListAdapter imageListAdapter;
    private FlickrAdapter flickrAdapter;
    private Thread flickrLoaderThread;
    private ListView imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().replace(R.id.container, new PlaceholderFragment()).commit();

        flickrAdapter = new FlickrAdapter(FLICKR_API_KEY, FLICKR_NASA_UID);
        flickrLoaderThread = new Thread(new Runnable() {
            public void run() {
                flickrAdapter.loadFlickrImages();
            }
        });

        flickrLoaderThread.start();

        imageListAdapter = new ImageListAdapter(this, flickrAdapter);
        imageListAdapter.setUpdateNotifier();

        imageList = (ListView) findViewById(R.id.listView);
        imageList.setAdapter(imageListAdapter);
    }

    @Override
    protected void onStop() {
        flickrLoaderThread.interrupt();
        super.onStop();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }
}
