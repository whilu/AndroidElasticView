package co.lujun.androidelasticview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;

import co.lujun.library.ElasticListView;
import co.lujun.library.ElasticRecycleView;
import co.lujun.library.ElasticScrollView;

/**
 * Created by lujun on 2015/8/13.
 */
public class MainActivity extends AppCompatActivity {

    ElasticScrollView mScrollView;
    ElasticRecycleView mRecyclerView;
    ElasticListView mListView;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        String[] strings = new String[]{
                "0------------0",
                "1------------1",
                "2------------2",
                "3------------3",
                "4------------4",
                "5------------5",
                "6------------6",
                "7------------7",
                "8------------8",
                "8------------9",
                "8------------10",
                "8------------11",
                "8------------12",
                "8------------13",
                "9------------14"
        };

        title = (TextView) findViewById(R.id.title);

        mScrollView = (ElasticScrollView) findViewById(R.id.scrollview);

        mListView = (ElasticListView) findViewById(R.id.listview);
        View headerView = LayoutInflater.from(this).inflate(R.layout.view_header, null, false);
        View footerView = LayoutInflater.from(this).inflate(R.layout.view_footer, null, false);
        mListView.addCustomHeader(headerView);
        mListView.addCustomFooter(footerView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, strings);
        mListView.setAdapter(adapter);

        mRecyclerView = (ElasticRecycleView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new MyAdapter(strings));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        title.setVisibility(View.GONE);
        if (id == R.id.action_listview) {
            mListView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
        }else if (id == R.id.action_scrollview){
            mListView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        }else if (id == R.id.action_recyclerview){
            mListView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        private String[] mStrs;

        public MyAdapter(String[] strings){
            mStrs = strings;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            ImageView imageView;

            public MyViewHolder(View v){
                super(v);
                imageView = (ImageView) v.findViewById(R.id.rv_imageview);
            }
        }

        @Override
        public int getItemCount() {
            return mStrs.length;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.view_rv_item, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            //bind data
        }
    }
}
