package co.lujun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lujun on 2015/8/13.
 */
public class ElasticRecycleView extends RecyclerView{

    private float mElasticFactor;// 弹性因子

    private float mOldX;// 起始X坐标

    private int mCurWidth; // 当前宽度

    private int mAnimDuration;// 动画时间

    private int mFirstVisibleItem; //第一个可见的Item

    private int mScrollState; // 滚动状态

    private int mCurPullState; // 拉动方向

    private boolean canPullLeft, canPullRight;// 可否左拉，右拉

    private boolean isMoved;// 是否移动了View

    private boolean isInRighter; // 是否滚到最右边

    private Timer mTimer; // 执行动画定时器

    private TimerTask mTimerTask; // 执行动画定时器任务

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PULL_LEFT){
                mCurWidth -= 20;
                if (mCurWidth > 0) {
                    setPadding(mCurWidth, 0, 0, 0);
                }else {
                    setPadding(0, 0, 0, 0);
                }
            }else if (msg.what == PULL_RIGHT){
                mCurWidth += 20;
                if (mCurWidth < 0){
                    setPadding(0, 0, -mCurWidth, 0);
                }else {
                    setPadding(0, 0, 0, 0);
                }
            }
        }
    };

    private static int PULL_LEFT = 0; // 左拉方向
    private static int PULL_RIGHT = 1; // 又拉方向

    public ElasticRecycleView(Context context) {
        super(context);
    }

    public ElasticRecycleView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public ElasticRecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticView);
        mElasticFactor = typedArray.getFloat(R.styleable.ElasticView_elastic_factor, 0.5f);
        mAnimDuration = 16;
        typedArray.recycle();

        this.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mScrollState = newState;
                if (getLayoutManager() instanceof LinearLayoutManager){
                    mFirstVisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                    int lastVisibleItem = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    int totalItemCount = getLayoutManager().getItemCount();
                    isInRighter = lastVisibleItem == totalItemCount - 1;
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                stopBackAnim();
                canPullLeft = isCanPullLeft();
                canPullRight = isCanPullRight();
                mOldX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                if (canPullLeft || canPullRight){
                    float newX = ev.getX();
                    int deltaX = (int) (newX - mOldX);

                    boolean shouldMove = (canPullRight && deltaX > 0)
                            || (canPullLeft && deltaX < 0)
                            || (canPullLeft && canPullRight);
                    if (shouldMove){
                        int offset = (int) (deltaX * mElasticFactor);
                        mCurWidth = offset;
                        if (deltaX < 0){
                            setPadding(0, 0, -offset, 0);
                            mCurPullState = PULL_RIGHT;
                        }else if (deltaX > 0){
                            setPadding(offset, 0, 0, 0);
                            mCurPullState = PULL_LEFT;
                        }
                        isMoved = true;
                    }
                }else {
                    mOldX = ev.getX();
                    canPullLeft = isCanPullLeft();
                    canPullRight = isCanPullRight();
                    mOldX = ev.getX();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isMoved){
                    break;
                }

                playBackAnim(mCurPullState);
                canPullRight = false;
                canPullLeft = false;
                isMoved = false;
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanPullRight(){
        return mFirstVisibleItem == 0;
    }

    private boolean isCanPullLeft(){
        return mScrollState == RecyclerView.SCROLL_STATE_IDLE && isInRighter ;
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
}
