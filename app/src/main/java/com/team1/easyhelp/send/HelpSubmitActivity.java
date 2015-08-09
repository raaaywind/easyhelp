package com.team1.easyhelp.send;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.utils.ActivityCollector;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class HelpSubmitActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText contentEdit;
    private EditText coinEdit;
    private EditText demandNumEdit;

    private int user_id;
    private String title;
    private String content;
    private int coinNum;
    private int demandNum;
    private double latitude;
    private double longitude;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_submit);
        // 收集该Activity，便于销毁
        ActivityCollector.getInstance().addActivity(this);

        initial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_submit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.confirm) {
            confirmHelpEvent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initial() {
        initialToolBar();
        initialLayout();
        initialUser();
    }

    // 绑定UI控件
    private void initialLayout() {
        titleEdit = (EditText) findViewById(R.id.title_edit);
        contentEdit = (EditText) findViewById(R.id.text_edit);
        coinEdit = (EditText) findViewById(R.id.love_coin_edit);
        demandNumEdit = (EditText) findViewById(R.id.demand_num_edit);
    }

    // 初始化ToolBar的功能
    private void initialToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("输入求助地点");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

        // 设置toolbar的NavigationIcon的点击响应事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取消本次求助
                ActivityCollector.getInstance().exit();
            }
        });
    }

    // 初始化已获得的用户信息
    private void initialUser() {
        user_id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        Bundle bundle = this.getIntent().getExtras();
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
        location = bundle.getString("location");
    }


    // 提交事件
    private void confirmHelpEvent() {
        title = titleEdit.getText().toString();
        content = contentEdit.getText().toString();
        if (coinEdit.getText().toString().equals("")) {
            coinNum = 0;
        } else {
            coinNum = Integer.parseInt(coinEdit.getText().toString());
        }
        if (demandNumEdit.getText().toString().equals("")) {
            demandNum = 0;
        } else {
            demandNum = Integer.parseInt(demandNumEdit.getText().toString());
        }

        if (title.equals("")) {
            Toast.makeText(HelpSubmitActivity.this, "事件的标题不能为空",
                    Toast.LENGTH_SHORT).show();
        } else {
            // 在后台线程上传事件信息到服务器
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String jsonString = "{" +
                            "\"id\":" + user_id + ",\"type\":1," +
                            "\"title\":\"" + title + "\"," +
                            "\"content\":\"" + content + "\"," +
                            "\"longitude\":" +  longitude + "," +
                            "\"latitude\":" + latitude + "," +
                            "\"location\":\"" + location + "\"," +
                            "\"love_coin\":" + coinNum + "," +
                            "\"demand_number\":" + demandNum + "}";
                    String message = RequestHandler.sendPostRequest(
                            "http://120.24.208.130:1501/event/add", jsonString);
                    if (message.equals("false")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "连接失败，请检查网络是否连接并重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        try {
                            JSONObject jO = new JSONObject(message);
                            if (jO.getInt("status") == 500) {
                                if (jO.getInt("value") == -2) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),
                                                    "爱心币余额不足，请重新设置悬赏金额",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "提交失败",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                ActivityCollector.getInstance().exit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}
