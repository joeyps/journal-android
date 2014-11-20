package com.thosedays.ui.widget;

/**
 * Created by joey on 14/11/19.
 */
public class IconListItem {

    public static final int INVALID_RESOURCE_ID = -1;

    private String mText;
    private int mIconResourceId;

    public IconListItem(String text) {
        mText = text;
        mIconResourceId = INVALID_RESOURCE_ID;
    }

    public IconListItem(String text, int iconResourceId) {
        mText = text;
        mIconResourceId = iconResourceId;
    }

    public String getText() {
        return mText;
    }

    public int getIconId() {
        return mIconResourceId;
    }
}
