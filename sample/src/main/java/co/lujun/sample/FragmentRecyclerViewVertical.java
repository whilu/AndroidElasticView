package co.lujun.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidelasticview.ElasticRecycleView;

/**
 * Created by lujun on 2015/8/14.
 */
public class FragmentRecyclerViewVertical extends Fragment {

    View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        init();
        return mView;
    }

    private void init(){
        if (mView == null){
            return;
        }


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ElasticRecycleView recycleView = (ElasticRecycleView) mView.findViewById(R.id.recyclerview);
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(linearLayoutManager);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 10; i++){
            list.add(i + "------" + i);
        }
        recycleView.setAdapter(new MyAdapter(list));
    }
}
