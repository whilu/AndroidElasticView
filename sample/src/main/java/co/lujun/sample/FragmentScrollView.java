package co.lujun.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lujun on 2015/8/14.
 */
public class FragmentScrollView extends Fragment {

    View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_scrollview, container, false);
        init();
        return mView;
    }

    private void init(){
        if (mView == null){
            return;
        }

    }
}
