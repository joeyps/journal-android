package com.thosedays.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.thosedays.provider.EventContract;
import com.thosedays.ui.widget.ObservableScrollView;
import com.thosedays.util.ImageLoader;

import joey.thosedays.R;

/**
 * Created by joey on 14/11/12.
 */
public class EventDetailFragment extends Fragment implements ObservableScrollView.Callbacks {

    private int mImageHeight;

    private ViewHolder viewHolder;
    private ObservableScrollView mScrollView;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);
        TextView textView = (TextView) view.findViewById(R.id.text_description);
        textView.setText(args.getString(EventContract.Events.EVENT_DESCRIPTION));
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollview);
        mScrollView.addCallbacks(this);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        String url = args.getString(EventContract.Events.PHOTO_URL);
        ImageView imageView = (ImageView) view.findViewById(R.id.img_photo);
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.imageView = imageView;
        }
        if (!TextUtils.isEmpty(url)) {
            url = url + "1000";
            int width = container.getWidth();
            int w = args.getInt(EventContract.Events.PHOTO_WIDTH);
            int h = args.getInt(EventContract.Events.PHOTO_HEIGHT);
            int height = (int) (((float) width) / w * h);
            imageView.setMinimumHeight(height);
            imageView.setTag(url);
            ImageLoader.load(viewHolder, url);
        } else {
            imageView.setTag(null);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mScrollView == null) {
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        if (scrollY < mImageHeight) {
            viewHolder.imageView.setTranslationY(scrollY/2);
        }
    }

    private void recomputePhotoAndScrollingMetrics() {
        mImageHeight = viewHolder.imageView.getMeasuredHeight();
    }

    private static class ViewHolder implements ImageLoader.ImageHolder {

        private ImageView imageView;
        private int width;

        @Override
        public void setImage(Bitmap bitmap, String key) {
            if (key.equals(imageView.getTag())) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(null);
            }
        }
    }
}
