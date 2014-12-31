/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thosedays.sync;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.thosedays.util.AccountUtils;
import com.thosedays.util.MediaUtils;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.LOGE;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 23/11/8.
 */
public class WebServiceApi {
    private static final String TAG = makeLogTag(WebServiceApi.class);

    public static final String API_EVENT = "/event";
    public static final String API_PHOTO = "/photo";

    private static final String PARAMETER_DATA = "data";
    private final Context mContext;
    private final String mUrl;
    private final String mAuthToken;

    public WebServiceApi(Context context, Account account) {
        mContext = context;
        mUrl = Config.API_URL;
        mAuthToken = AccountUtils.getAuthToken(context, account, Config.AUTH_TYPE);
    }

    /**
     * Posts a session to the event server.
     *
     * @param api The ID of the session that was reviewed.
     * @return whether or not updating succeeded
     */
    public String sendDataToServer(String api, Map<String, Object> data) {

        BasicHttpClient httpClient = new BasicHttpClient();
        httpClient.addHeader(Config.HEADER_AUTHORIZATION, "access_token=" + mAuthToken);

        Gson gson = new Gson();
        ParameterMap parameterMap = httpClient.newParams();
        parameterMap.add(PARAMETER_DATA, gson.toJson(data));

        HttpResponse response = httpClient.post(mUrl + api, parameterMap);

        if (response != null && response.getStatus() == HttpURLConnection.HTTP_OK) {
            LOGD(TAG, "Server returned HTTP_OK, so session posting was successful.");
            return response.getBodyAsString();
        } else {
            LOGE(TAG, "Error posting session: HTTP status " + response.getStatus());
            return null;
        }
    }

    public String deleteDataFromServer(String api, String id) {

        BasicHttpClient httpClient = new BasicHttpClient();
        httpClient.addHeader(Config.HEADER_AUTHORIZATION, "access_token=" + mAuthToken);

        HttpResponse response = httpClient.delete(String.format("%s%s/%s", mUrl, api, id), null);

        if (response != null && response.getStatus() == HttpURLConnection.HTTP_OK) {
            LOGD(TAG, "Server returned HTTP_OK, so session posting was successful.");
            return response.getBodyAsString();
        } else {
            LOGE(TAG, "Error posting session: HTTP status " + response.getStatus());
            return null;
        }
    }

    /**
     * Posts a session to the event server.
     *
     * @param api The ID of the session that was reviewed.
     * @return whether or not updating succeeded
     */
    public String sendFileToServer(String api, Uri uri) {
        LOGD(TAG, "sendFileToServer api=" + api + " uri=" + uri);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        String boundary =  "*****";
        String crlf = "\r\n";
        String twoHyphens = "--";
        int maxBufferSize = 1*1024*1024;
        try
        {
            File file = MediaUtils.getFile(mContext, uri);
            LOGD(TAG, "file=" + file.getAbsolutePath());
            final InputStream imageStream = new FileInputStream(file);

            URL url = new URL(mUrl + api);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs and Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");
            connection.setRequestProperty(Config.HEADER_AUTHORIZATION, "access_token=" + mAuthToken);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            LOGD(TAG, "Content-Disposition: form-data; name=\"files\";filename=\"" + file.getAbsolutePath().getBytes("UTF-8") +"\"" + crlf);
            //Start content wrapper
            outputStream = new DataOutputStream( connection.getOutputStream() );
            outputStream.writeBytes(twoHyphens + boundary + crlf);
            //workaround for chinese encoding error
            outputStream.writeBytes("Content-Disposition: form-data; name=\"files\";filename=\"" + file.getAbsolutePath().getBytes("UTF-8") +"\"" + crlf);
            outputStream.writeBytes(crlf);

            int bytesAvailable = imageStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // Read file
            int bytesRead = imageStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = imageStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = imageStream.read(buffer, 0, bufferSize);
            }
            //End content wrapper
            outputStream.writeBytes(crlf);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            final StringBuilder sb  = new StringBuilder();
            if (serverResponseCode == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream );
                BufferedReader br = new BufferedReader(isReader );
                String s = null;
                while ((s = br.readLine()) != null) {
                    sb.append(s);
                }
            }

            imageStream.close();
            outputStream.flush();
            outputStream.close();
            connection.disconnect();
            return sb.toString();
        } catch (Exception e) {
            //Exception handling
            e.printStackTrace();
        } finally {
            //TODO release all resources if exception has been thrown
        }
        return null;
    }
}
