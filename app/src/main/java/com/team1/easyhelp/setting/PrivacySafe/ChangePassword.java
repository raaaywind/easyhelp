package com.team1.easyhelp.setting.PrivacySafe;

/**
 * Created by kyouzuimine on 15/8/13.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.account.LoginActivity;
import com.team1.easyhelp.utils.ActivityCollector;
import com.team1.easyhelp.utils.MD5;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;


public class ChangePassword extends AppCompatActivity {
    private String phone;
    private TextView phone_number;
    private EditText old_Password;
    private EditText new_password1;
    private EditText new_password2;
    private String jsonString;
    private String message;
    private int user_id;
    String Password;
    String N_Password1;
    String N_Password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        old_Password = (EditText)findViewById(R.id.old_password);
        new_password1 = (EditText)findViewById(R.id.new_password1);
        new_password2 = (EditText)findViewById(R.id.new_password2);
        old_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        new_password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        new_password2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        Intent intent = getIntent();
        ActivityCollector.getInstance().addActivity(this);

        //get the information of sendhelp_map location
        Bundle bunde = this.getIntent().getExtras();
        phone = bunde.getString("phone").toString();
        //user_id = bunde.getInt("user_id");
        phone_number = (TextView)findViewById(R.id.phone_number);
        phone_number.setText(phone);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_password, menu);
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

    public boolean submit(View v) {
        Password = old_Password.getText().toString();
        N_Password1 = new_password1.getText().toString();
        N_Password2 = new_password2.getText().toString();
        if (N_Password1 == null || N_Password2 == null || old_Password == null) {
            Toast.makeText(getApplicationContext(), "请输入完整信息",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Password.length() < 6 || N_Password1.length() < 6 || N_Password2.length() < 6) {
            Toast.makeText(getApplicationContext(), "请至少输入6位密码",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!N_Password1.equals(N_Password2)) {
            Toast.makeText(getApplicationContext(), "新密码不一致，请重新输入",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        new Thread(runnable).start();
        return true;
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //执行验证
            verify();
        }
    };

    private void verify() {
            jsonString = "{" +
                    "\"account\":\"" + phone + "\"," +
                    "\"password\":\"\"" +  "}";
            message = RequestHandler.sendPostRequest(
                    "http://120.24.208.130:1501/account/login", jsonString);
            if (message == "false") {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            String salt;
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "用户未注册",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                salt = jO.getString("salt"); //密码加密的盐值
                //MD5加密
                String password2 = MD5.MD5_encode(MD5.MD5_encode(Password, ""), salt);
                jsonString = "{" +
                        "\"account\":\"" + phone + "\"," +
                        "\"password\":\"" + password2 + "\"," +
                        "\"salt\":\"" + salt + "\" " +  "}";
                message = RequestHandler.sendPostRequest(
                        "http://120.24.208.130:1501/account/login", jsonString);
                if (message == "false") {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                if (jO.getInt("status") == 500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "密码错误，请重新输入密码",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                } else {
                        //修改用户的密码
                        String salt1;
                        salt1 = jO.getString("salt"); //密码加密的盐值
                        //MD5加密
                        String new_password = MD5.MD5_encode(MD5.MD5_encode(N_Password1, ""), salt1);
                        jsonString = "{" +
                                "\"account\":\"" + phone + "\"," +
                                "\"password\":\"" + new_password + "\"," +
                                "\"salt\":\"" + salt1 + "\" " +  "}";
                        message = RequestHandler.sendPostRequest(
                                "http://120.24.208.130:1501/account/modify_password", jsonString);
                        if (message == "false") {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        jO = new JSONObject(message);
                        if (jO.getInt("status") == 500) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "提交密码与当前密码相同，修改失败",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "修改成功,请重新登录",
                                            Toast.LENGTH_SHORT).show();
                                    //修改完跳转到登陆界面，退出登陆状态重新登陆
                                    SharedPreferences sharedPreferences = ChangePassword.this
                                            .getSharedPreferences("user_id", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("user_id", -1);
                                    editor.commit();

                                    Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
                                    startActivity(intent);
                                    ActivityCollector.getInstance().exit();
                                    // 销毁该页面
                                    ChangePassword.this.finish();
                                }
                            });

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }
}