package co.lujun.androidelasticview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by lujun on 2015/8/13.
 *
 * Origin article: http://www.2cto.com/kf/201402/279066.html
 */
public class ElasticScrollView extends ScrollView {

    private float mElasticFactor;// 弹性因子

    private float mOldY;// 起始Y坐标

    private int mAnimDuration;// 动画时间

    private int originTop, originLeft, originBottom, originRight;// 起始子View的上左下右坐标

    private View mContentView;// 子View

    private boolean canPullUp, canPullDown;// 可否上拉，下拉

    private boolean isMoved;// 是否移动了View

    public ElasticScrollView(Context context){
        super(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public ElasticScrollView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticView);
        mElasticFactor = typedArray.getFloat(R.styleable.ElasticView_elastic_factor, 0.5f);
        mAnimDuration = typedArray.getInt(R.styleable.ElasticView_anim_duration, 300);
        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1){
            throw new RuntimeException(getClass().getSimpleName() + ":must only have one child view!");
        }
        mContentView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mContentView != null){
            originTop = mContentView.getTop();
            originLeft = mContentView.getLeft();
            originBottom = mContentView.getBottom();
            originRight = mContentView.getRight();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
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
                        mContentView.layout(originLeft, originTop + offset,
                                originRight, originBottom + offset);
                        isMoved = true;
                    }
                }else {
                    mOldY = ev.getY();
                    canPullDown = isCanPullDown();
                    mOldY = ev.getY();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isMoved){
                    break;
                }
                AnimationSet animationSet = new AnimationSet(true);
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0,
                        mContentView.getTop(), originTop);
                translateAnimation.setDuration(mAnimDuration);
                translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                animationSet.addAnimation(translateAnimation);
                mContentView.startAnimation(animationSet);

                mContentView.layout(originLeft, originTop, originRight, originBottom);

                canPullDown = false;
                canPullUp = false;
                isMoved = false;
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanPullDown(){
        return getScrollY() == 0;
    }

    private boolean isCanPullUp(){
        return mContentView.getHeight() <= getScrollY() + getHeight();
    }
}
