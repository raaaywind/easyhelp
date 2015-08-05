package com.team1.easyhelp.account;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.team1.easyhelp.R;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.SMSSDK;
import cn.smssdk.EventHandler;

public class RegisterActivity extends AppCompatActivity {

    private EditText phoneNumEdit;
    private EditText requestCodeEdit;
    private Button submitButton;

    private String phoneNum;

    public final static String EXTRA_MESSAGE = "com.ehelp.MESSAGE";

    public final static int GET_REQUESTCODE = 0;
    public final static int WAITING_REQUEST = 1;
    public final static int SUBMIT_REQUESTCODE = 2;
    private int flag; // flag为0时按钮具有获取验证码响应，flag为1时按钮具有提交验证码响应
    int i = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initial();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    // 销毁SMSSDK，避免内存泄露
    @Override
    protected void onDestroy() {
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }


    // 进行UI控件绑定以及验证码模块的初始化操作
    private void initial() {
        // 使用严苛模式
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().
                        detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().
                detectLeakedSqlLiteObjects().detectLeakedClosableObjects().
                penaltyLog().penaltyDeath().build());

        phoneNumEdit = (EditText) findViewById(R.id.edit_phoneNum);
        requestCodeEdit = (EditText) findViewById(R.id.edit_request_code);
        submitButton = (Button) findViewById(R.id.button_submit);
        flag = 0;

        initSMS();
    }

    // 初始化短信验证SDK
    private void initSMS() {
        // 使用严苛模式
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().
                        detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().
                detectLeakedSqlLiteObjects().detectLeakedClosableObjects().
                penaltyLog().penaltyDeath().build());

        SMSSDK.initSDK(this, "8e8e104c2348", "a5a3c12216e6df81397844b0a73fb2db");
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);

        setRequestEditChangeAction();
        setSubmitClickAction();
    }


    // 根据验证码输入框的状态响应相应的动作
    private void setRequestEditChangeAction() {
        requestCodeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (requestCodeEdit.getText().toString().isEmpty()) {
                    if (flag == 0) {
                        changeSubmitButtonState(GET_REQUESTCODE);
                    } else {
                        changeSubmitButtonState(WAITING_REQUEST);
                    }
                } else {
                    // requestCodeEdit不为空的时候按键响应提交验证码的事件
                    changeSubmitButtonState(SUBMIT_REQUESTCODE);
                    flag = 1;
                }
            }
        });
    }


    // 设置按钮对应的响应
    private void setSubmitClickAction() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNum = phoneNumEdit.getText().toString();
                if (phoneNum.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请输入手机号",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (flag == 0) {
                        String jsonString = "{" +
                                "\"account\":\"" + phoneNum + "\"," +
                                "\"password\":\"\"" + "}";
                        String message = RequestHandler.sendPostRequest(
                                "http://120.24.208.130:1501/account/login", jsonString
                        );
                        if (message.equals("false")) {
                            Toast.makeText(getApplicationContext(), "连接失败，请检查网络是否连接并重试",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject jO = new JSONObject(message);
                            if (jO.getInt("status") == 200) {
                                Toast.makeText(getApplicationContext(), "该用户已注册",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if (!judgePhoneNums(phoneNum)) // 检查手机号格式是否正确
                                    return;
                                SMSSDK.getVerificationCode("86", phoneNum); // 操作SMSSDK发送短信验证
                                requestCodeEdit.setHint("重新发送(" + i + ")");
                                changeSubmitButtonState(WAITING_REQUEST);
                                flag = 1; // 改变点击按键的响应事件，使之相应提交验证过程
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (; i > 0; i--) {
                                            if (flag == 0)
                                                break;
                                            handler.sendEmptyMessage(-9);
                                            if (i <= 0)
                                                break;
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        handler.sendEmptyMessage(-8);
                                        flag = 0; // 如果超过规定时间则将按键响应事件改回到发送验证码状态
                                    }
                                }).start();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String requestCode = requestCodeEdit.getText().toString();
                        SMSSDK.submitVerificationCode("86", phoneNum, requestCode);
                    }
                }
            }
        });
    }


    // 设置按钮的显示状态和Clickable属性
    private void changeSubmitButtonState(int id) {
        if (id == GET_REQUESTCODE) {
            submitButton.setClickable(true);
            submitButton.setBackground(getResources().
                    getDrawable(R.drawable.login_button_actionup_shape));
            submitButton.setText("获取验证码");
        } else if (id == WAITING_REQUEST) {
            submitButton.setClickable(false);
            submitButton.setBackground(getResources().
                    getDrawable(R.drawable.login_button_actiondown_shape));
            submitButton.setText("等待验证码发送");
        } else if (id == SUBMIT_REQUESTCODE) {
            submitButton.setClickable(true);
            submitButton.setBackground(getResources().
                    getDrawable(R.drawable.login_button_actionup_shape));
            submitButton.setText("提交验证");
        }
    }


    /**
     * 判断手机号码是否合理
     *
     * @param phoneNums
     */
    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11)
                && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "手机号码输入有误！",Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 判断一个字符串的位数
     * @param str
     * @param length
     * @return boolean
     */
    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length;
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobileNums) {
        /*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return !TextUtils.isEmpty(mobileNums) && mobileNums.matches(telRegex);
    }

    /**
     * 检查验证码提交结果
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                requestCodeEdit.setHint("重新发送(" + i + ")");
            } else if (msg.what == -8) {
                requestCodeEdit.setHint("请输入验证码");
                changeSubmitButtonState(GET_REQUESTCODE);
                i = 60;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                        Toast.makeText(getApplicationContext(), "验证码提交成功",
                                Toast.LENGTH_SHORT).show();
                        setPassword();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        submitButton.setClickable(true);
                        submitButton.setBackground(getResources().
                                getDrawable(R.drawable.login_button_actiondown_shape));
                        Toast.makeText(getApplicationContext(), "验证码已发送",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                } else {
                    flag = 0; // 验证码验证失败后回复到情求验证码状态
                    changeSubmitButtonState(GET_REQUESTCODE);
                    requestCodeEdit.setText("");
                    requestCodeEdit.clearFocus();
                    Toast.makeText(getApplicationContext(), "验证码错误，请重新获取验证码",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // 跳转到设定密码页面
    public void setPassword() {
        Intent intent = new Intent(this, setPasswordActivity.class);
        intent.putExtra(EXTRA_MESSAGE, phoneNum);
        startActivity(intent);
        RegisterActivity.this.finish();
    }

}
