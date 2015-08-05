package com.team1.easyhelp.account;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.home.HomeActivity;
import com.team1.easyhelp.utils.MD5;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    protected ImageButton loginButton;

    private EditText accountEdit;
    private EditText passwordEdit;
    private SharedPreferences sharedPref;

    private String account;
    private String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initial_layout();
        check_if_confirmed();
    }

    // 初始化界面布局以及动画效果
    public void initial_layout() {
        loginButton = (ImageButton) findViewById(R.id.imageButton);

        // 设置按下以后按钮背景改变
        loginButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    loginButton.setBackground(getResources().
                            getDrawable(R.drawable.login_button_actiondown_shape));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    loginButton.setBackground(getResources().
                            getDrawable(R.drawable.login_button_actionup_shape));
                }
                return false;
            }
        });
    }

    // 查询登录状态，如果已有登陆的user_id，则跳过登陆验证界面，去到主页
    public void check_if_confirmed() {
        sharedPref = this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        int id = sharedPref.getInt("user_id", -1);
        if (id != -1) {
            Intent it = new Intent(this, HomeActivity.class);
            startActivity(it);
            LoginActivity.this.finish();
        }
    }

    // 设置按下登陆按钮后的验证过程
    public void Login(View view) {
        accountEdit = (EditText) findViewById(R.id.usertext);
        passwordEdit = (EditText) findViewById(R.id.passwordtext);
        account = accountEdit.getText().toString();
        password = passwordEdit.getText().toString();

        new Thread(loginRunnable).start();
    }

    public void register(View view) {
        Intent it = new Intent(this, RegisterActivity.class);
        startActivity(it);
    }

    public void findPassword(View view) {
        Intent it = new Intent(this, FindPasswordActivity.class);
        startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            String message;
            String url;
            String salt;
            int user_id;

            if (account.isEmpty() || password.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "登陆信息不完整",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String jsonString = "{" +
                        "\"account\":\"" + account + "\"," +
                        "\"password\":\"\"" +  "}";
                url = "http://120.24.208.130:1501/account/login";
                message = RequestHandler.sendPostRequest(url, jsonString);

                if (message.equals("false")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "连接失败，请检查网络连接",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        if (jsonObject.getInt("status") == 500) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "用户未注册",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        salt = jsonObject.getString("salt");
                        password = MD5.MD5_encode(MD5.MD5_encode(password, ""), salt);
                        jsonString = "{" +
                                "\"account\":\"" + account + "\"," +
                                "\"password\":\"" + password + "\"," +
                                "\"salt\":\"" + salt + "\" " +  "}";
                        message = RequestHandler.sendPostRequest(url, jsonString);
                        if (message.equals("false")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        jsonObject = new JSONObject(message);
                        if (jsonObject.getInt("status") == 500) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "密码错误，登录失败",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            jsonObject = new JSONObject(message);
                            user_id = jsonObject.getInt("id");
                            url = "http://120.24.208.130:1501/user/get_information";
                            jsonString = "{" +
                                    "\"id\":" + user_id + "}";
                            message = RequestHandler.sendPostRequest(url, jsonString);
                            if (message.equals("false")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                jsonObject = new JSONObject(message);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("user_id", user_id);
                                editor.putString("account", account);
                                if (jsonObject.getString("nickname").isEmpty())
                                    editor.putString("nickname", account);
                                else
                                    editor.putString("nickname", jsonObject.getString("nickname"));
                                editor.commit();

                                Intent it = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(it);
                                LoginActivity.this.finish();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };
}
