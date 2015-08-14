package co.lujun.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import co.lujun.library.ElasticListView;

/**
 * Created by lujun on 2015/8/14.
 */
public class FragmentListView extends Fragment {

    View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_listview, container, false);
        init();
        return mView;
    }

    private void init(){
        if (mView == null){
            return;
        }
        String[] strings = new String[]{
                "1-----------1",
                "2-----------2",
                "3-----------3",
                "4-----------4",
                "5-----------5",
                "6-----------6",
                "7-----------7",
                "8-----------8",
                "9-----------9",
                "10-----------10",
                "11-----------11",
                "12-----------12",
                "13-----------13"
        };

        ElasticListView listView = (ElasticListView) mView.findViewById(R.id.listview);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.view_header, null, false);
        View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.view_footer, null, false);
        listView.addCustomHeader(headerView);
        listView.addCustomFooter(footerView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(adapter);
    }
}
