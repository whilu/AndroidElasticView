package co.lujun.androidelasticview.listener;

/**
 * Created by lujun on 2015/9/21.
 */
public interface OnOffsetChangedListener {
    /**
     *
     * @param type 0-下拉，1-上拉
     * @param offset 偏移量
     */
    void onOffsetChanged(int type, int offset);
}
