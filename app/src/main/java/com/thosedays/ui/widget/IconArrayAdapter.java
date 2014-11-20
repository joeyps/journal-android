package com.thosedays.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by joey on 14/11/19.
 */
public class IconArrayAdapter extends ArrayAdapter<IconListItem> {

    private Context mContext;
    private int mLayoutResourceId;
    private int mTextViewResourceId;
    private int mImageViewResourceId;

    public IconArrayAdapter(Context context, int resource, int textViewResourceId,
                            int imageViewResourceId, IconListItem[] objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
        mLayoutResourceId = resource;
        mTextViewResourceId = textViewResourceId;
        mImageViewResourceId = imageViewResourceId;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        IconListItem item = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResourceId, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(mTextViewResourceId);
            holder.imageView = (ImageView) convertView.findViewById(mImageViewResourceId);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.textView.setText(item.getText());
        if (item.getIconId() != IconListItem.INVALID_RESOURCE_ID)
            holder.imageView.setImageResource(item.getIconId());

        return convertView;
    }
}
