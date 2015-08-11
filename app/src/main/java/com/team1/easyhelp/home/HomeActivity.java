package com.team1.easyhelp.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.team1.easyhelp.R;
import com.team1.easyhelp.home.fragment.ViewPagerFragment;
import com.team1.easyhelp.home.fragment.HomeFragment;
import com.team1.easyhelp.send.HelpMapActivity;
import com.team1.easyhelp.send.QuestionSendActivity;
import com.team1.easyhelp.send.TransitionActivity;
import com.team1.easyhelp.testActivity;

import br.liveo.Model.HelpLiveo;
import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.navigationliveo.NavigationLiveo;



public class HomeActivity extends NavigationLiveo {

    private HelpLiveo mHelpLiveo;

    private String nickname;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInt(Bundle savedInstanceState) {
        initialUserInfo();
        initialMenuItems();
    }

    // 初始化用户信息，并将其显示在左划菜单视图上
    private void initialUserInfo() {
        SharedPreferences sharedPref =
                this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        nickname = sharedPref.getString("nickname", "");

        // 将用户信息更新在左侧菜单栏上
        this.userName.setText(nickname);
        this.userBackground.setImageResource(R.drawable.ic_user_background_second);
        this.userPhoto.setImageResource(R.drawable.ic_account_circle_grey_200_48dp);
    }

    // 设定菜单栏上面的列表项
    private void initialMenuItems() {
        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add("主页", R.drawable.ic_home_black_24dp);
        mHelpLiveo.add("个人主页", R.drawable.ic_assessment_black_24dp);
        mHelpLiveo.add("通讯录", R.drawable.ic_friend_list_black_24dp);
        mHelpLiveo.add("健康卡", R.drawable.ic_health_card_black_24dp);
        mHelpLiveo.add("爱心银行", R.drawable.ic_local_atm_black_24dp);
        mHelpLiveo.add("我参与的", R.drawable.ic_visibility_black_24dp);
        mHelpLiveo.addSeparator();
        mHelpLiveo.add("帮助", R.drawable.ic_help_black_24dp);

        with(new MyListener()).startingPosition(0).addAllHelpItem(mHelpLiveo.getHelp())
                .colorItemCounter(R.color.nliveo_red_colorPrimary)
                .footerItem("设置", R.drawable.ic_settings_black_24dp)
                .setOnPrepareOptionsMenu(onPrepare)
                .setOnClickFooter(onClickFooter)
                .setOnClickUser(onClickPhoto)
                .build();

        int position = this.getCurrentPosition();
        this.setElevationToolBar(position != 0 ? 15 : 0);
    }

    private OnPrepareOptionsMenuLiveo onPrepare = new OnPrepareOptionsMenuLiveo() {
        @Override
        public void onPrepareOptionsMenu(Menu menu, int position, boolean visible) {
        }
    };

    private View.OnClickListener onClickPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "onClickPhoto :D", Toast.LENGTH_SHORT).show();
            closeDrawer();
        }
    };

    private View.OnClickListener onClickFooter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), testActivity.class));
            closeDrawer();
        }
    };

    class MyListener implements OnItemClickListener {
        @Override
        public void onItemClick(int position) {
            Fragment mFragment;
            FragmentManager mFragmentManager = getSupportFragmentManager();
            switch (position) {
                case 0:
                    mFragment = new ViewPagerFragment();
                    break;
                default:
                    mFragment = HomeFragment.newInstance(mHelpLiveo.get(position).getName());
            }
            if (mFragment != null){
                mFragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
            }
            setElevationToolBar(position != 0 ? 15 : 0);
        }
    }
}
