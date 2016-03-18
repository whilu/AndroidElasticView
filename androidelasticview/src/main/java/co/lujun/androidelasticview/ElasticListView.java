package co.lujun.androidelasticview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Timer;
import java.util.TimerTask;

import co.lujun.androidelasticview.listener.GestureDetectorListener;
import co.lujun.androidelasticview.listener.OnDirectionChangedListener;
import co.lujun.androidelasticview.listener.OnGestureChangedListener;
import co.lujun.androidelasticview.listener.OnOffsetChangedListener;

/**
 * Created by lujun on 2015/8/13.
 */
public class ElasticListView extends ListView implements AbsListView.OnScrollListener {

    private float mElasticFactor = 0.5f;// 弹性因子

    private float mOldY;// 起始Y坐标

    private int mHeaderHeight, mFooterHeight = 50;// header height和footer height(dp)

    private int mCurHeight; // 当前高度

    private int mAnimDuration = 400;// 动画时间

    private int mFirstVisibleItem; //第一个可见的Item

    private int mScrollState; // 滚动状态

    private int mCurPullState; // 拉动方向

    private View mHeaderView, mFooterView;//header view和footer view

    private boolean canPullUp, canPullDown;// 可否上拉，下拉

    private boolean isMoved;// 是否移动了View

    private boolean isInited;// 是否初始化结束

    private boolean isInFooter; // 是否滚到底部

    private boolean isUp;// 按下是否松开

    private OnOffsetChangedListener mListener; //偏移量变化监听

    private GestureDetectorListener mGestureDetectorListener;// GestureDetector.SimpleOnGestureListener

    private GestureDetector mGestureDetector;// GestureDetector

    private OnGestureChangedListener mGestureChangedListener;// OnGestureChangedListener

    private ValueAnimator.AnimatorUpdateListener mPullDownAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float val = (Float) animation.getAnimatedValue();
            mHeaderView.setPadding(0, (int) val, 0, 0);
        }
    };

    private ValueAnimator.AnimatorUpdateListener mPullUpAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float val = (Float) animation.getAnimatedValue();
            mFooterView.setPadding(0, 0, 0, (int) val);
        }
    };

    private static int PULL_DOWN = 0; // 下拉方向
    private static int PULL_UP = 1; // 上拉方向

    public ElasticListView(Context context) {
        super(context);
    }

    public ElasticListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public ElasticListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticView);
        mElasticFactor = typedArray.getFloat(R.styleable.ElasticView_elastic_factor, mElasticFactor);
        mAnimDuration = typedArray.getInt(R.styleable.ElasticView_anim_duration, mAnimDuration);
        mFooterHeight = (int)typedArray.getDimension(R.styleable.ElasticView_footer_height, dp2px(context, mFooterHeight));
        typedArray.recycle();
        setOnScrollListener(this);
        mGestureDetectorListener = new GestureDetectorListener(this);
        mGestureDetectorListener.setOnGestureDealListener(new GestureDetectorListener.GestureDealListener() {
            @Override
            public void dealGesture(int i) {
                if (mGestureChangedListener != null) {
                    mGestureChangedListener.onGestureChanged(i);
                }
            }
        });
        mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeaderView == null || mFooterView == null){
            throw new RuntimeException(getClass().getSimpleName()
                    + ":HeaderView and FooterView can't be null!");
        }
        if (!isInited){
            mHeaderHeight = mHeaderView.getHeight();
//            mFooterHeight = mFooterView.getHeight();
            mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
//            mFooterView.setPadding(0, 0, 0, -mFooterHeight);
            isInited = true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                isUp = false;
                canPullUp = isCanPullUp();
                canPullDown = isCanPullDown();
                mOldY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (canPullUp || canPullDown){
                    float newY = ev.getY();
                    int deltaY = (int) (newY - mOldY);

                    boolean shouldMove = (canPullDown && deltaY > 0)
                            || (canPullUp && deltaY < 0)
                            || (canPullUp && canPullDown);
                    if (shouldMove){
                        int offset;
                        if (deltaY < 0){
                            offset = (int) (deltaY * mElasticFactor);
                        }else {
                            offset = (int) (deltaY * mElasticFactor - mHeaderHeight);
                        }
                        mCurHeight = offset;
                        if (deltaY < 0){
                            if (mListener != null){
                                mListener.onOffsetChanged(1, offset);
                            }
                            mFooterView.setPadding(0, 0, 0, -offset);
                            mCurPullState = PULL_UP;
                        }else if (deltaY > 0){
                            if (mListener != null){
                                mListener.onOffsetChanged(0, offset);
                            }
                            mHeaderView.setPadding(0, offset, 0, 0);
                            mCurPullState = PULL_DOWN;
                        }
                        isMoved = true;
                    }
                }else {
                    mOldY = ev.getY();
                    canPullUp = isCanPullUp();
                    canPullDown = isCanPullDown();
                    mOldY = ev.getY();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isUp = true;
                if (!isMoved){
                    break;
                }

                playBackAnim(mCurPullState);
                canPullDown = false;
                canPullUp = false;
                isMoved = false;
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        isInFooter = totalItemCount > 0 && mFirstVisibleItem + visibleItemCount == totalItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
    }

    private boolean isCanPullDown(){
        return mFirstVisibleItem == 1 || mFirstVisibleItem == 0;
    }

    private boolean isCanPullUp(){
        return mScrollState == SCROLL_STATE_IDLE && isInFooter;
    }

    private void playBackAnim(final int flag){
        if (flag == PULL_DOWN){
            ObjectAnimator animator = ObjectAnimator
                    .ofFloat(mHeaderView, "", mCurHeight, -mHeaderHeight).setDuration(mAnimDuration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(mPullDownAnimatorListener);
            animator.start();
            return;
        }else if (flag == PULL_UP){
            ObjectAnimator animator = ObjectAnimator
                    .ofFloat(mFooterView, "", -mCurHeight, -mFooterHeight).setDuration(mAnimDuration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(mPullUpAnimatorListener);
            animator.start();
            return;
        }
    }

    public void hideHeader(boolean anim){
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(mHeaderView, "", mCurHeight, -mHeaderHeight).setDuration(anim ? mAnimDuration : 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(mPullDownAnimatorListener);
        animator.start();
    }

    public void addCustomHeader(View v){
        this.addHeaderView(v);
        mHeaderView = v;
    }

    public void addCustomFooter(View v){
        this.addFooterView(v);
        mFooterView = v;
    }

    public void setOffsetChangeListener(OnOffsetChangedListener listener){
        this.mListener = listener;
    }

    public void setScrollDirectionListener(OnDirectionChangedListener listener){
        this.mGestureDetectorListener.setGestureDirectionListener(listener);
    }

    public void setGestureChangedListener(OnGestureChangedListener listener){
        this.mGestureChangedListener = listener;
    }

    public boolean isUp(){
        return this.isUp;
    }

    public void setIsUp(boolean isUp){
        this.isUp = isUp;
    }

    /**
     * 设置获取滑动方向过程的纠错抽取数目
     * @param num
     */
    public void setErrorContainerNum(int num){
        this.mGestureDetectorListener.setErrorContainerNum(num);
    }

    public float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

}
