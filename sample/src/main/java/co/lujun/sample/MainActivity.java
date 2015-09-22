package co.lujun.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    Fragment curFragment;
    FragmentManager fragmentManager;
    Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragments = new Fragment[]{
                new FragmentListView(),
                new FragmentScrollView()
        };
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().add(R.id.content_frame, fragments[0]).commit();
            curFragment = fragments[0];
        }
    }

    private void replaceFragment(Fragment from, Fragment to){
        if (from == null || to == null){
            return;
        }
        if (curFragment != to) {
            if (!to.isAdded()) {
                fragmentManager.beginTransaction().hide(from).add(R.id.content_frame, to).commit();
            } else {
                fragmentManager.beginTransaction().hide(from).show(to).commit();
            }
            curFragment = to;
        }
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
        if (id == R.id.action_listview) {
            replaceFragment(curFragment, fragments[0]);
            curFragment = fragments[0];
        }else if (id == R.id.action_scrollview) {
            replaceFragment(curFragment, fragments[1]);
            curFragment = fragments[1];
        }

        return super.onOptionsItemSelected(item);
    }
}
