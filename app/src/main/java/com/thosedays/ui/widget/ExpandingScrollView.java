package com.thosedays.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/12/2.
 */
public class ExpandingScrollView extends LinearLayout {
    private static final String TAG = makeLogTag(ExpandingScrollView.class);

    /** These are used for computing child frames based on their gravity. */
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();

    private int mTouchSlop;
    private boolean mIsScrolling = false;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;

    private float mInitialMotionY = 0;

    private final Rect mContentRect = new Rect();

    public ExpandingScrollView(Context context) {
        super(context);
        init(context);
    }

    public ExpandingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExpandingScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void addAnchorPoint() {

    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mIsScrolling = false;
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mScroller = new OverScroller(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LOGD("joey", "onSizeChanged w=" + w + " h=" + h + " oldw=" + oldw + " oldh=" + oldh);
        LOGD("joey", "onSizeChanged getWidth()=" + getWidth() + " getHeight()=" +getHeight());
        mContentRect.set(0, 0, w, h / 2);
//        mContentRect.set(
//                getPaddingLeft() + mMaxLabelWidth + mLabelSeparation,
//                getPaddingTop(),
//                getWidth() - getPaddingRight(),
//                getHeight() - getPaddingBottom() - mLabelHeight - mLabelSeparation);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        // Never allow swiping to switch between pages
        final int action = MotionEventCompat.getActionMasked(ev);
        LOGD(TAG, "onInterceptTouchEvent action=" + action);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                int N = getChildCount();
                for (int i = 0; i < N; i++) {
                    if (isPointInsideView(ev.getRawX(), ev.getRawY(), getChildAt(i))) {
                        LOGD(TAG, "start drag");
                        mIsScrolling = true;
                        return true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsScrolling) {
                    // We're currently scrolling
                    return true;
                }

                final int yDiff = (int) Math.abs(mInitialMotionY - ev.getY());
                if (yDiff > mTouchSlop) {
                    View view = getChildAt(0);
                    if (isPointInsideView(ev.getRawX(), ev.getRawY(), view)) {
                        // Start scrolling!
                        mIsScrolling = true;
                        return true;
                    }
                }
                break;
            }
        }
        // In general, we don't want to intercept touch events. They should be
        // handled by the child view.
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsScrolling)
            return false;
        final int action = MotionEventCompat.getActionMasked(ev);
//        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
//        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        boolean retVal = mGestureDetector.onTouchEvent(ev);
//        LOGD(TAG, "onTouchEvent action=" + MotionEventCompat.getActionMasked(ev));
        LOGD(TAG, "onTouchEvent action=" + MotionEventCompat.getActionMasked(ev) + " retVal=" + retVal);

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{
                mIsScrolling = false;
                break;
            }
        }

        return retVal || super.onTouchEvent(ev);
    }

    /**
     * Determines if given points are inside view
     * @param x - x coordinate of point
     * @param y - y coordinate of point
     * @param view - view object to compare
     * @return true if the points are within view bounds, false otherwise
     */
    private static boolean isPointInsideView(float x, float y, View view){
        LOGD(TAG, "isPointInsideView x=" + x + " y=" + y);
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        LOGD(TAG, "view viewX=" + viewX + " viewY=" + viewY);

        //point is inside view bounds
        if(( x > viewX && x < (viewX + view.getWidth())) &&
                ( y > viewY && y < (viewY + view.getHeight()))){
            return true;
        } else {
            return false;
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // Never allow swiping to switch between pages
//        if (mIsScrolling)
//            return true;
//        return false;
//    }

    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
//            releaseEdgeEffects();
//            mScrollerStartViewport.set(mCurrentViewport);
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(ExpandingScrollView.this);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            mZoomer.forceFinished(true);
//            if (hitTest(e.getX(), e.getY(), mZoomFocalPoint)) {
//                mZoomer.startZoom(ZOOM_AMOUNT);
//            }
//            ViewCompat.postInvalidateOnAnimation(InteractiveLineGraphView.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            LOGD("joey", "[scroll] onScroll");
            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            /**
             * Pixel offset is the offset in screen pixels, while viewport offset is the
             * offset within the current viewport. For additional information on surface sizes
             * and pixel offsets, see the docs for {@link computeScrollSurfaceSize()}. For
             * additional information about the viewport, see the comments for
             * {@link mCurrentViewport}.
             */
            LOGD("joey", "[scroll] distanceX=" + distanceX + " distanceY=" + distanceY);
            int scrollY = getScrollY();
            boolean canScrollX = false;
            boolean canScrollY = true;
            int dx = canScrollX ? (int)distanceX : 0;
            int dy = canScrollY ? (int)distanceY : 0;
            if (canScrollX || canScrollY) {
                if ((scrollY + dy) > mContentRect.height()) {
                    dy = mContentRect.height() - scrollY;
                }

                if ((scrollY + dy) < 0) {
                    dy = 0 - scrollY;
                }
                scrollBy(dx, dy);
            }
//            float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
//            float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
//            computeScrollSurfaceSize(mSurfaceSizeBuffer);
//            int scrolledX = (int) (mSurfaceSizeBuffer.x
//                    * (mCurrentViewport.left + viewportOffsetX - AXIS_X_MIN)
//                    / (AXIS_X_MAX - AXIS_X_MIN));
//            int scrolledY = (int) (mSurfaceSizeBuffer.y
//                    * (AXIS_Y_MAX - mCurrentViewport.bottom - viewportOffsetY)
//                    / (AXIS_Y_MAX - AXIS_Y_MIN));


//            setViewportBottomLeft(
//                    mCurrentViewport.left + viewportOffsetX,
//                    mCurrentViewport.bottom + viewportOffsetY);
//
//            if (canScrollX && scrolledX < 0) {
//                mEdgeEffectLeft.onPull(scrolledX / (float) mContentRect.width());
//                mEdgeEffectLeftActive = true;
//            }
//            if (canScrollY && scrolledY < 0) {
//                mEdgeEffectTop.onPull(scrolledY / (float) mContentRect.height());
//                mEdgeEffectTopActive = true;
//            }
//            if (canScrollX && scrolledX > mSurfaceSizeBuffer.x - mContentRect.width()) {
//                mEdgeEffectRight.onPull((scrolledX - mSurfaceSizeBuffer.x + mContentRect.width())
//                        / (float) mContentRect.width());
//                mEdgeEffectRightActive = true;
//            }
//            if (canScrollY && scrolledY > mSurfaceSizeBuffer.y - mContentRect.height()) {
//                mEdgeEffectBottom.onPull((scrolledY - mSurfaceSizeBuffer.y + mContentRect.height())
//                        / (float) mContentRect.height());
//                mEdgeEffectBottomActive = true;
//            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LOGD("joey", "[scroll] onFling");
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    private void fling(int velocityX, int velocityY) {
//        releaseEdgeEffects();
//        // Flings use math in pixels (as opposed to math based on the viewport).
//        computeScrollSurfaceSize(mSurfaceSizeBuffer);
//        mScrollerStartViewport.set(mCurrentViewport);
//        int startX = (int) (mSurfaceSizeBuffer.x * (mScrollerStartViewport.left - AXIS_X_MIN) / (
//                AXIS_X_MAX - AXIS_X_MIN));
//        int startY = (int) (mSurfaceSizeBuffer.y * (AXIS_Y_MAX - mScrollerStartViewport.bottom) / (
//                AXIS_Y_MAX - AXIS_Y_MIN));
        mScroller.forceFinished(true);
        mScroller.fling(
                getScrollX(),
                getScrollY(),
                velocityX,
                velocityY,
                0, 0,
                0, mContentRect.height(),
                0,
                0);
//        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        LOGD("joey", "[scroll] computeScroll");
        boolean needsInvalidate = false;

        if (mScroller.computeScrollOffset()) {

            // The scroller isn't finished, meaning a fling or programmatic pan operation is
            // currently active.

            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();
            LOGD("joey", "[scroll] currX=" + currX + " currY=" + currY);

            scrollTo(currX, currY);
            needsInvalidate = true;
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        LOGD("joey", "onMeasure w=" + widthMeasureSpec + " h=" + heightMeasureSpec);
        LOGD("joey", "suggest w=" + MeasureSpec.getSize(widthMeasureSpec) + " h=" + MeasureSpec.getSize(heightMeasureSpec));
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
//        // Try for a width based on our minimum
//        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
//
//        // Whatever the width ends up being, ask for a height that would let the pie
//        // get as big as it can
//        int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
//        int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)mTextWidth, heightMeasureSpec, 0);
//
//        setMeasuredDimension(w, h);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
        setMeasuredDimension(w, h*2);
    }

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LOGD("joey", "onLayout changed="+ changed + " l=" + left + " t=" + top + " r=" + right + " b=" + bottom);
        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();
        LOGD("joey", "leftPos" + leftPos + " rightPos=" + rightPos);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int height = child.getMeasuredHeight();
                LOGD("joey", "child" + i + " height=" + height);
                // Place the child.
                child.layout(leftPos, height,
                        rightPos, height * 2);
            }
        }
    }
}
