package com.team1.easyhelp.account;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.mapapi.SDKInitializer;
import com.team1.easyhelp.R;
import com.team1.easyhelp.testActivity;

public class PreloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preload);

        initial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preload, menu);
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

    private void initial() {
        // 在开始应用时初始化地图控件
        SDKInitializer.initialize(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1100);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        check_if_confirmed();
                    }
                });
            }
        }).start();
    }

    // 查询登录状态，如果已有登陆的user_id，则跳过登陆验证界面，去到主页
    public void check_if_confirmed() {
        int id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        if (id != -1) {
            Intent it = new Intent(this, testActivity.class);
            startActivity(it);
        } else {
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
        }
        PreloadActivity.this.finish();
    }
}
