package com.thosedays.sync;

import android.accounts.Account;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.thosedays.model.DataManifest;
import com.thosedays.util.AccountUtils;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.LOGE;
import static com.thosedays.util.LogUtils.LOGW;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/7.
 */
public class RemoteDataFetcher {

    private static final String TAG = makeLogTag(RemoteDataFetcher.class);
    private static final String MANIFEST_FORMAT = "manifest-json-v1";

    // timestamp of the manifest file on the server
    private String mServerTimestamp = null;
    private String mModifiedSince = null;

    private String mManifestUrl;
    private String mAuthToken;

    public RemoteDataFetcher(Context context, Account account) {
        mAuthToken = AccountUtils.getAuthToken(context, account, Config.AUTH_TYPE);
        mManifestUrl = Config.MANIFEST_URL;
    }

    /**
     * Fetches data from the remote server.
     *
     * @param refTimestamp The timestamp of the data to use as a reference; if the remote data
     *                     is not newer than this timestamp, no data will be downloaded and
     *                     this method will return null.
     *
     * @return The data downloaded, or null if there is no data to download
     * @throws java.io.IOException if an error occurred during download.
     */
    public RawData[] fetchDataIfNewer(String refTimestamp) throws IOException {
        if (TextUtils.isEmpty(mManifestUrl)) {
            LOGW(TAG, "Manifest URL is empty (remote sync disabled!).");
            return null;
        }
        mModifiedSince = refTimestamp;

        BasicHttpClient httpClient = new BasicHttpClient();
        httpClient.addHeader(Config.HEADER_AUTHORIZATION, "access_token=" + mAuthToken);
        //httpClient.setRequestLogger(mQuietLogger);

        // Only download if data is newer than refTimestamp
        // Cloud Storage is very picky with the If-Modified-Since format. If it's in a wrong
        // format, it refuses to serve the file, returning 400 HTTP error. So, if the
        // refTimestamp is in a wrong format, we simply ignore it. But pay attention to this
        // warning in the log, because it might mean unnecessary data is being downloaded.
        if (!TextUtils.isEmpty(refTimestamp)) {
            if (Config.isValidFormatForIfModifiedSinceHeader(refTimestamp)) {
                httpClient.addHeader(Config.HEADER_MODIFIED_SINCE, refTimestamp);
                LOGD(TAG, "Sync timestamp since " + refTimestamp);
            } else {
                LOGW(TAG, "Could not set If-Modified-Since HTTP header. Potentially downloading " +
                        "unnecessary data. Invalid format of refTimestamp argument: "+refTimestamp);
            }
        }

        HttpResponse response = httpClient.get(mManifestUrl, null);
        if (response == null) {
            LOGE(TAG, "Request for manifest returned null response.");
            throw new IOException("Request for data manifest returned null response.");
        }

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            LOGD(TAG, "Server returned HTTP_OK, so new data is available.");
            mServerTimestamp = getLastModified(response);
            LOGD(TAG, "Server timestamp for new data is: " + mServerTimestamp);
            String body = response.getBodyAsString();
            if (TextUtils.isEmpty(body)) {
                LOGE(TAG, "Request for manifest returned empty data.");
                throw new IOException("Error fetching conference data manifest: no data.");
            }
            LOGD(TAG, "Manifest "+mManifestUrl+" read, contents: " + body);
            //mBytesDownloaded += body.getBytes().length;
            return processManifest(body);
        } else if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
            // data on the server is not newer than our data
            LOGD(TAG, "HTTP_NOT_MODIFIED: data has not changed since " + refTimestamp);
            return null;
        } else {
            LOGE(TAG, "Error fetching conference data: HTTP status " + status);
            throw new IOException("Error fetching conference data: HTTP status " + status);
        }
    }

    /**
     * Process the data manifest and download data files referenced from it.
     * @param manifestJson The JSON of the manifest file.
     * @return The contents of the set of files referenced from the manifest, or null
     * if none could be retrieved.
     * @throws IOException If an error occurs while retrieving information.
     */
    private RawData[] processManifest(String manifestJson) throws IOException {
        LOGD(TAG, "Processing data manifest, length " + manifestJson.length());

        DataManifest manifest = new Gson().fromJson(manifestJson, DataManifest.class);
        if (manifest.format == null || !manifest.format.equals(MANIFEST_FORMAT)) {
            LOGE(TAG, "Manifest has invalid format spec: " + manifest.format);
            throw new IOException("Invalid format spec on manifest:" + manifest.format);
        }

        if (manifest.data_files == null || manifest.data_files.length == 0) {
            LOGW(TAG, "Manifest does not list any files. Nothing done.");
            return null;
        }

        LOGD(TAG, "Manifest lists " + manifest.data_files.length + " data files.");
        RawData[] rawDatas = new RawData[manifest.data_files.length];
        for (int i = 0; i < manifest.data_files.length; i++) {
            String url = manifest.data_files[i].data;
            LOGD(TAG, "Processing data file: " + sanitizeUrl(url));
            String data = fetchFile(url);
            if (TextUtils.isEmpty(data)) {
                LOGE(TAG, "Failed to fetch data file: " + sanitizeUrl(url));
                throw new IOException("Failed to fetch data file " + sanitizeUrl(url));
            }
            rawDatas[i] = new RawData();
            rawDatas[i].type = manifest.data_files[i].type;
            rawDatas[i].data = data;
        }

        LOGD(TAG, "Got " + rawDatas.length + " data files.");
//        cleanUpCache();
        return rawDatas;
    }

    /**
     * Fetches a file from the cache/network, from an absolute or relative URL. If the
     * file is available in our cache, we read it from there; if not, we will
     * download it from the network and cache it.
     *
     * @param url The URL to fetch the file from. The URL may be absolute or relative; if
     *            relative, it will be considered to be relative to the manifest URL.
     * @return The contents of the file.
     * @throws IOException If an error occurs.
     */
    private String fetchFile(String url) throws IOException {
        // If this is a relative url, consider it relative to the manifest URL
        if (!url.contains("://")) {
            if (TextUtils.isEmpty(mManifestUrl) || !mManifestUrl.contains("/")) {
                LOGE(TAG, "Could not build relative URL based on manifest URL.");
                return null;
            }
            int i = mManifestUrl.lastIndexOf('/');
            url = mManifestUrl.substring(0, i) + "/" + url;
        }

        LOGD(TAG, "Attempting to fetch: " + sanitizeUrl(url));

        // Check if we have it in our cache first
        String body = null;
//        try {
////            body = loadFromCache(url);
//            if (!TextUtils.isEmpty(body)) {
//                // cache hit
////                mBytesReadFromCache += body.getBytes().length;
////                mCacheFilesToKeep.add(getCacheKey(url));
//                return body;
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            LOGE(TAG, "IOException getting file from cache.");
//            // proceed anyway to attempt to download it from the network
//        }

        // We don't have the file on cache, so download it
        LOGD(TAG, "Cache miss. Downloading from network: " + sanitizeUrl(url));
        BasicHttpClient client = new BasicHttpClient();
        client.addHeader(Config.HEADER_AUTHORIZATION, "access_token=" + mAuthToken);
        client.addHeader(Config.HEADER_MODIFIED_SINCE, mModifiedSince);
        client.addHeader(Config.HEADER_LAST_MODIFIED, mServerTimestamp);
//        client.setRequestLogger(mQuietLogger);
        HttpResponse response = client.get(url, null);

        if (response == null) {
            throw new IOException("Request for URL " + sanitizeUrl(url) + " returned null response.");
        }

        LOGD(TAG, "HTTP response " + response.getStatus());
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            body = response.getBodyAsString();
            if (TextUtils.isEmpty(body)) {
                throw new IOException("Got empty response when attempting to fetch " +
                        sanitizeUrl(url));
            }
            LOGD(TAG, "Successfully downloaded from network: " + sanitizeUrl(url));
//            mBytesDownloaded += body.getBytes().length;
//            writeToCache(url, body);
//            mCacheFilesToKeep.add(getCacheKey(url));
            return body;
        } else {
            LOGE(TAG, "Failed to fetch from network: " + sanitizeUrl(url));
            throw new IOException("Request for URL " + sanitizeUrl(url) +
                    " failed with HTTP error " + response.getStatus());
        }
    }

    // Sanitize a URL for logging purposes (only the last component is left visible).
    private String sanitizeUrl(String url) {
        int i = url.lastIndexOf('/');
        if (i >= 0 && i < url.length()) {
            return url.substring(0, i).replaceAll("[A-za-z]", "*") +
                    url.substring(i);
        }
        else return url.replaceAll("[A-za-z]", "*");
    }

    private String getLastModified(HttpResponse resp) {
        if (!resp.getHeaders().containsKey(Config.HEADER_LAST_MODIFIED)) {
            return "";
        }

        List<String> s = resp.getHeaders().get(Config.HEADER_LAST_MODIFIED);
        return s.isEmpty() ? "" : s.get(0);
    }

    // Returns the timestamp of the data downloaded from the server
    public String getServerDataTimestamp() {
        return mServerTimestamp;
    }
}
