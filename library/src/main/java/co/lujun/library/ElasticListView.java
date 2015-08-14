package co.lujun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lujun on 2015/8/13.
 */
public class ElasticListView extends ListView implements AbsListView.OnScrollListener {

    private float mElasticFactor;// 弹性因子

    private float mOldY;// 起始Y坐标

    private int mHeaderHeight, mFooterHeight;// header height和footer height

    private int mCurHeight; // 当前高度

    private int mAnimDuration;// 动画时间

    private int mFirstVisibleItem; //第一个可见的Item

    private int mScrollState; // 滚动状态

    private int mCurPullState; // 拉动方向

    private View mHeaderView, mFooterView;//header view和footer view

    private boolean canPullUp, canPullDown;// 可否上拉，下拉

    private boolean isMoved;// 是否移动了View

    private boolean isInited;// 是否初始化结束

    private boolean isInFooter; // 是否滚到底部

    private Timer mTimer; // 执行动画定时器

    private TimerTask mTimerTask; // 执行动画定时器任务

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PULL_DOWN){
                mCurHeight -= 20;
                if (mCurHeight > -mHeaderHeight) {
                    mHeaderView.setPadding(0, mCurHeight, 0, 0);
                }else {
                    mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
                }
            }else if (msg.what == PULL_UP){
                mCurHeight += 20;
                if (mCurHeight < 0){
                    mFooterView.setPadding(0, 0, 0, -mCurHeight);
                }else {
                    mFooterView.setPadding(0, 0, 0, -mFooterHeight);
                }
            }
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
        mElasticFactor = typedArray.getFloat(R.styleable.ElasticView_elastic_factor, 0.5f);
        mAnimDuration = 16;
        typedArray.recycle();
        setOnScrollListener(this);
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
            mFooterHeight = mFooterView.getHeight();
            mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
            mFooterView.setPadding(0, 0, 0, -mFooterHeight);
            isInited = true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                stopBackAnim();
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
                        int offset = (int) (deltaY * mElasticFactor);
                        mCurHeight = offset;
                        if (deltaY < 0){
                            mFooterView.setPadding(0, 0, 0, -offset);
                            mCurPullState = PULL_UP;
                        }else if (deltaY > 0){
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
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(flag);
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, mAnimDuration);

    }

    private void stopBackAnim(){
        if (mTimer != null){
            if (mTimerTask != null){
                mTimerTask.cancel();
            }
            mTimer.cancel();
        }
    }

    public void addCustomHeader(View v){
        this.addHeaderView(v);
        mHeaderView = v;
    }

    public void addCustomFooter(View v){
        this.addFooterView(v);
        mFooterView = v;
    }
}
