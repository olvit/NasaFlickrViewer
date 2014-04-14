package com.applications.flickrviewer.app.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkConnector {

    private static final String LOG_TAG = "NetworkConnector";
    private static final int HTTP_REQUEST_TIMEOUT_MS = 50 * 1000;
    private static final int HTTP_STATUS_OK = 200;

    private HttpURLConnection connection;

    public NetworkConnector() {
    }

    public String GetRequest(String urlStr) {
        InputStream inStream = null;
        String serverResponce = null;

        try {
            URL url = new URL(urlStr);

            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT_MS);
            connection.setConnectTimeout(HTTP_REQUEST_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();

            if (connection.getResponseCode() == HTTP_STATUS_OK) {
                inStream = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"), 8);
                StringBuilder strBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    strBuilder.append(line).append("\n");
                }

                serverResponce = strBuilder.toString();
            } else {
                Log.e(LOG_TAG, "Fail to connect to (" + urlStr + "), HTTP error code: " + connection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to perform request (" + urlStr + "): " + e.getMessage());
        } finally {

            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to close Input Stream: " + e.getMessage());
                }
            }

            connection.disconnect();
        }

        return serverResponce;
    }

    public void downloadToFile(String urlStr, File file) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            URL url = new URL(urlStr);

            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(HTTP_REQUEST_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            connection.connect();

            if (connection.getResponseCode() == HTTP_STATUS_OK) {
                inStream = connection.getInputStream();
                outStream = new FileOutputStream(file);

                try {
                    byte[] buffer = new byte[1024];

                    int length;
                    while ((length = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to download content: " + e.getMessage());
                }
            } else {
                Log.e(LOG_TAG, "Fail to connect to (" + urlStr + "), HTTP error code: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to perform request (" + urlStr + "): " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to close Input Stream: " + e.getMessage());
                }
            }

            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to close Output Stream: " + e.getMessage());
                }
            }

            connection.disconnect();
        }
    }
}
