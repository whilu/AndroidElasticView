package co.lujun.androidelasticview.listener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidelasticview.ElasticListView;

/**
 * GestureDetector.SimpleOnGestureListener接口
 * Created by lujun on 2015/3/31.
 */
public class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener
        implements View.OnTouchListener {

    private GestureDealListener mGestureDealListener;
    private OnDirectionChangedListener mGestureDirectionListener;
    private float axis_y;
    private boolean isScrollToUp; // 是否向上滚动
    private int mScollFirstItem = 0; // listview中第一项索引
    private int mScreenY = 0; // listview第一项在屏幕中的位置
    private List<Boolean> directionContainer;// 容错处理容器

    private int container_size = 3; // 获取活动方向容错抽取数目

    private AbsListView mListView;

    public GestureDetectorListener(AbsListView listView){
        this.mListView = listView;
        directionContainer = new ArrayList<Boolean>();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
        float y = e2.getY() - e1.getY();
        if(y > 0){// Slide to bottom
            if (mGestureDealListener != null) {
                mGestureDealListener.dealGesture(1);
            }
        }else if (y < 0) {// Slide to top
            if (mGestureDealListener != null) {
                mGestureDealListener.dealGesture(0);
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event){
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event){

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("debugss", "in" + distanceX);
        if (mListView.getChildCount() > 0) {
            boolean scrollToUp = false;
            View firstChild = mListView.getChildAt(mListView.getFirstVisiblePosition());
            int[] location = new int[2];
            if (firstChild != null){
                firstChild.getLocationOnScreen(location);

                if (mScollFirstItem != mListView.getFirstVisiblePosition()){
                    if (mListView.getFirstVisiblePosition() > mScollFirstItem){
                        scrollToUp = true;// 向上滑动
                    }else {
                        scrollToUp = false;// 向下滑动
                    }
                    mScollFirstItem = mListView.getFirstVisiblePosition();
                    mScreenY = location[1];
                }else {
                    if (mScreenY > location[1]){
                        scrollToUp = true; // 向上滑动
                    }else if (mScreenY < location[1]){
                        scrollToUp = false;// 向下滑动
                    }
                    mScreenY = location[1];
                }
                /*if (directionContainer.size() < container_size){
                    directionContainer.add(scrollToUp);
                }else {
                    int true_sum = 0;
                    int false_sum = 0;
                    for (Boolean boo : directionContainer) {
                        if (boo){
                            true_sum++;
                        }else {
                            false_sum++;
                        }
                    }
                    if (true_sum > false_sum){
                        scrollToUp = true;
//                        onScrollDirectionChanged(true);
                    }else if (true_sum < false_sum){
                        scrollToUp = false;
//                        onScrollDirectionChanged(false);
                    }
                    if (isScrollToUp != scrollToUp){
                        isScrollToUp = scrollToUp;
                        onScrollDirectionChanged(isScrollToUp);
                    }
                    directionContainer.clear();
                }*/
                /*if (isScrollToUp != scrollToUp){
                    isScrollToUp = scrollToUp;
                    onScrollDirectionChanged(isScrollToUp);
                }*/
            }
        }
        return false;
    }

    private void onScrollDirectionChanged(boolean isScrollToUp){
        if (mGestureDirectionListener != null){
            mGestureDirectionListener.onDirectionChanged(isScrollToUp);
        }
    }

    @Override
    public void onShowPress(MotionEvent event){

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event){
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {
            axis_y = event.getY();
            return true;
        }
        if (event.getAction() == event.ACTION_UP) {// 0-Slide to top, 1-Slide to bottom, 2-Slide to left, 3-Slide to right
            if (event.getY()-axis_y < 0) {// Slide to top
                if (mGestureDealListener != null) {
                    mGestureDealListener.dealGesture(0);
                }
            }else {// Slide to bottom
                if (mGestureDealListener != null) {
                    mGestureDealListener.dealGesture(1);
                }
            }
        }
        return true;
    }

    /**
     * 设置手势处理回调接口
     * @param gesture
     */
    public void setOnGestureDealListener(GestureDealListener gesture){
        this.mGestureDealListener = gesture;
    }

    public void setGestureDirectionListener(OnDirectionChangedListener listener){
        this.mGestureDirectionListener = listener;
    }

    /** 手势处理回调接口*/
    public interface GestureDealListener{
        void dealGesture(int i);
    }

    public void setErrorContainerNum(int num){
        if (num < 0){
            return;
        }else if (num != 0 && num % 2 == 0){
            num += 1;
        }
        this.container_size = num;
    }
}