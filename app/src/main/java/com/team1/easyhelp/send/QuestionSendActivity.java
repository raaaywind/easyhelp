package com.team1.easyhelp.send;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class QuestionSendActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText coinEdit;
    private EditText textEdit;

    private String title;
    private String text;
    private int coin;

    private int user_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send);

        initialLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_question_send, menu);
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
            // 获取用户输入的信息
            title = titleEdit.getText().toString();
            text = textEdit.getText().toString();
            if (coinEdit.getText().toString().equals("")) {
                coin = 0;
            } else {
                coin = Integer.parseInt(coinEdit.getText().toString());
            }
            // 点击toolbar提交按钮则提交信息到服务器
            submitQuestion();
        }

        return super.onOptionsItemSelected(item);
    }

    // 初始化页面布局
    private void initialLayout() {
        // 绑定UI控件
        titleEdit = (EditText) findViewById(R.id.title_edit);
        coinEdit = (EditText) findViewById(R.id.love_coin_edit);
        textEdit = (EditText) findViewById(R.id.text_edit);

        // 获取user_id
        user_id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        initialToolBar();
    }

    // 初始化Toolbar组件
    private void initialToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("发送提问");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

        // 设置toolbar的NavigationIcon的返回动作
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionSendActivity.this.finish();
            }
        });
    }

    // 提交用户输入的信息
    private void submitQuestion() {
        if (!title.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String jsonString = "{" +
                            "\"id\":" + user_id + ",\"type\":0," +
                            "\"title\":\"" + title + "\"," +
                            "\"content\":\"" + text + "\"," +
                            "\"love_coin\":" + coin + "}";
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
                                            Toast.makeText(getApplicationContext(), "爱心币不足",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                // 返回到调用提问功能的上一页Activity
                                QuestionSendActivity.this.finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            Toast.makeText(QuestionSendActivity.this, "问题标题不能为空",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
