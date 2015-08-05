package com.team1.easyhelp.account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.utils.MD5;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText passwordEdit;
    private EditText passwordRepeatEdit;
    private Button submitButton;

    private String account;
    private String password;
    private String password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reset_password, menu);
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
        passwordEdit = (EditText) findViewById(R.id.edit_password);
        passwordRepeatEdit = (EditText) findViewById(R.id.edit_password2);
        submitButton = (Button) findViewById(R.id.button_submit);
        setButtonTouchAction();
    }

    // 设置按钮按下时的响应动画
    private void setButtonTouchAction() {
        submitButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    submitButton.setBackground(getResources().
                            getDrawable(R.drawable.login_button_actiondown_shape));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    submitButton.setBackground(getResources().
                            getDrawable(R.drawable.login_button_actionup_shape));
                }
                return false;
            }
        });
    }

    // 提交修改或创建的密码
    public void submitPasswordClick(View view) {
        Intent intent = getIntent();
        account = intent.getStringExtra(FindPasswordActivity.EXTRA_MESSAGE);

        password = passwordEdit.getText().toString();
        password2 = passwordRepeatEdit.getText().toString();

        new Thread(resetPasswordRunnable).start();
    }

    Runnable resetPasswordRunnable = new Runnable() {
        @Override
        public void run() {
            String message;
            String jsonString;
            String salt;
            if (password.isEmpty() || password2.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请输入密码",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (password.length() < 6) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "请输入至少6位密码",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                if (!password.equals(password2)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "密码不一致",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                jsonString = "{" +
                        "\"account\":\"" + account + "\"," +
                        "\"password\":\"\"" +  "}";
                message = RequestHandler.sendPostRequest(
                        "http://120.24.208.130:1501/account/login", jsonString);
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

                try {
                    JSONObject jO = new JSONObject(message);
                    if (jO.getInt("status") == 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "用户名未注册",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    salt = jO.getString("salt");
                    password2 = MD5.MD5_encode(MD5.MD5_encode(password, ""), salt);
                    jsonString = "{" +
                            "\"account\":\"" + account + "\"," +
                            "\"password\":\"" + password2 + "\"," +
                            "\"salt\":\"" + salt + "\" " +  "}";
                    message = RequestHandler.sendPostRequest(
                            "http://120.24.208.130:1501/account/modify_password", jsonString);
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

                    jO = new JSONObject(message);
                    if (jO.getInt("status") == 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "与原密码相同，请设置新的密码", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "修改成功,请重新登录",
                                        Toast.LENGTH_SHORT).show();
                                ResetPasswordActivity.this.finish();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
