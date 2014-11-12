package com.thosedays.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by joey on 14/11/11.
 */
public class ImageLoader {

    public static String load(ImageHolder imageHolder, String strUrl) {
        URL url = null;
        try {
            String key = strUrl;
            url = new URL(strUrl);
            new SimpleImageLoader(imageHolder, key).execute(url);
            return key;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static interface ImageHolder {
        public void setImage(Bitmap bitmap, String key);
    }

    private static class SimpleImageLoader extends AsyncTask<URL, Void, Bitmap> {
        private ImageHolder mImageHolder;
        private String mKey;

        public SimpleImageLoader(ImageHolder imageHolder, String key) {
            mImageHolder = imageHolder;
            mKey = key;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            Bitmap bmp = null;
            try{
                HttpURLConnection con = (HttpURLConnection)urls[0].openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
            } catch(Exception e) {
            }

            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImageHolder.setImage(result, mKey);
        }
    }
}
