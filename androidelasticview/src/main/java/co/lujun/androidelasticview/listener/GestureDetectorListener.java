package co.lujun.androidelasticview.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * GestureDetector.SimpleOnGestureListener接口
 * Created by lujun on 2015/3/31.
 */
public class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener
        implements View.OnTouchListener {

    private GestureDealListener mGestureDealListener;
    private float axis_y;

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
        return false;
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

    /** 手势处理回调接口*/
    public interface GestureDealListener{
        void dealGesture(int i);
    }
}