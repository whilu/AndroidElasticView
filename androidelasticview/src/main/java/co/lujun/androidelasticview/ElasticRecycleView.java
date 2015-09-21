package co.lujun.androidelasticview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lujun on 2015/8/13.
 */
public class ElasticRecycleView extends RecyclerView{

    private float mElasticFactor;// 弹性因子

    private float mOldX, mOldY;// 起始X, Y坐标

    private int mCurDimen; // 当前宽度

    private int mAnimDuration;// 动画时间

    private int mFirstVisibleItem; //第一个可见的Item

    private int mScrollState; // 滚动状态

    private int mCurPullState; // 拉动方向

    private int mOrientation; // LinearLayoutManager方向

    private int lastVisibleItem;// 可见的总item数目

    private int totalItemCount; // 列表中的item总数目

    private boolean canPullOne, canPullTwo;// 可否左(上)拉，右(下)拉

    private boolean isMoved;// 是否移动了View

    private boolean isInRightOrBottom; // 是否滚到最右边

    private Timer mTimer; // 执行动画定时器

    private TimerTask mTimerTask; // 执行动画定时器任务

    private LayoutParams params1, params2;//

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PULL_LEFT){
                mCurDimen -= 20;
                if (mCurDimen > 0) {
                    params1.setMargins(mCurDimen, 0, 0, 0);
//                    setPadding(mCurDimen, 0, 0, 0);
                }else {
                    params1.setMargins(0, 0, 0, 0);
//                    setPadding(0, 0, 0, 0);
                }
                getChildAt(0).setLayoutParams(params1);
            }else if (msg.what == PULL_RIGHT){
                mCurDimen += 20;
                if (mCurDimen < 0){
                    params2.setMargins(0, 0, -mCurDimen, 0);
//                    setPadding(0, 0, -mCurDimen, 0);
                }else {
                    params2.setMargins(0, 0, 0, 0);
//                    setPadding(0, 0, 0, 0);
                }
                getChildAt(lastVisibleItem - mFirstVisibleItem).setLayoutParams(params2);
            }else if (msg.what == PULL_DOWN){
                mCurDimen -= 20;
                if (mCurDimen > 0) {
                    params1.setMargins(0, mCurDimen, 0, 0);
//                    setPadding(0, mCurDimen, 0, 0);
                }else {
                    params1.setMargins(0, 0, 0, 0);
//                    setPadding(0, 0, 0, 0);
                }
                getChildAt(0).setLayoutParams(params1);
            }else if (msg.what == PULL_UP){
                mCurDimen += 20;
                if (mCurDimen < 0){
                    params2.setMargins(0, 0, 0, -mCurDimen);
//                    setPadding(0, 0, 0, -mCurDimen);
                }else {
                    params2.setMargins(0, 0, 0, 0);
//                    setPadding(0, 0, 0, 0);
                }
                getChildAt(lastVisibleItem - mFirstVisibleItem).setLayoutParams(params2);
            }
        }
    };

    private static int PULL_LEFT = 0; // 左拉方向
    private static int PULL_RIGHT = 1; // 又拉方向
    private static int PULL_UP = 2; // 上拉方向
    private static int PULL_DOWN = 3; // 下拉方向

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

        setHasFixedSize(false);

        this.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mScrollState = newState;
                if (getLayoutManager() instanceof LinearLayoutManager){
                    mFirstVisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                    lastVisibleItem = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    totalItemCount = getLayoutManager().getItemCount();
                    isInRightOrBottom = lastVisibleItem == totalItemCount - 1;
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0){
            Log.d(getClass().getSimpleName(), "There is no child view!");
            return super.dispatchTouchEvent(ev);
        }
        if (getLayoutManager() instanceof  LinearLayoutManager){
            mOrientation = ((LinearLayoutManager) getLayoutManager()).getOrientation();
        }
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                stopBackAnim();
                canPullOne = isCanPullOne();
                canPullTwo = isCanPullTwo();
                mOldX = ev.getX();
                mOldY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (canPullOne || canPullTwo){
                    float newX = ev.getX();
                    float newY = ev.getY();
                    int deltaX = (int) (newX - mOldX);
                    int deltaY = (int) (newY- mOldY);

                    boolean shouldMove = (canPullTwo && (deltaY > 0))
                            || (canPullOne && (deltaY < 0))
                            || (canPullOne && canPullTwo);
                    if (shouldMove){
                        if (params1 == null) {
                            params1 = (LayoutParams) getChildAt(0).getLayoutParams();
                        }
                        if (lastVisibleItem == totalItemCount - 1 && params2 == null){
                            params2 = (LayoutParams) getChildAt(lastVisibleItem - mFirstVisibleItem).getLayoutParams();
                        }
                        if (mOrientation == HORIZONTAL){
                            int offset = (int) (deltaX * mElasticFactor);
                            mCurDimen = offset;
                            if (deltaX < 0){
                                params2.setMargins(0, 0, -offset, 0);
                                getChildAt(lastVisibleItem - mFirstVisibleItem).setLayoutParams(params2);
//                                setPadding(0, 0, -offset, 0);
                                mCurPullState = PULL_RIGHT;
                            }else if (deltaX > 0){
                                params1.setMargins(offset, 0, 0, 0);
                                getChildAt(0).setLayoutParams(params1);
//                                setPadding(offset, 0, 0, 0);
                                mCurPullState = PULL_LEFT;
                            }
                        }else {
                            int offset = (int) (deltaY * mElasticFactor);
                            mCurDimen = offset;
                            if (deltaY < 0){
                                params2.setMargins(0, 0, 0, -offset);
                                getChildAt(lastVisibleItem - mFirstVisibleItem).setLayoutParams(params2);
//                                setPadding(0, 0, 0, -offset);
                                mCurPullState = PULL_UP;
                            }else if (deltaY > 0){
                                params1.setMargins(0, offset, 0, 0);
                                getChildAt(0).setLayoutParams(params1);
//                                setPadding(0, offset, 0, 0);
                                mCurPullState = PULL_DOWN;
                            }
                        }
                        isMoved = true;
                    }
                }else {
                    canPullOne = isCanPullOne();
                    canPullTwo = isCanPullTwo();
                    mOldX = ev.getX();
                    mOldY = ev.getY();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isMoved){
                    break;
                }

                playBackAnim(mCurPullState);
                canPullTwo = false;
                canPullOne = false;
                isMoved = false;
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanPullTwo(){
        return mFirstVisibleItem == 0;
    }

    private boolean isCanPullOne(){
        return mScrollState == RecyclerView.SCROLL_STATE_IDLE && isInRightOrBottom ;
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
