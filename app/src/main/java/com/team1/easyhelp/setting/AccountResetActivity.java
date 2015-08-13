package com.team1.easyhelp.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.User;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 在这个页面内账户与安全的功能，主要分为密码重置与绑定手机号重置
 */
public class AccountResetActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int user_id = 21;
    private TextView Account;
    private User user;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().
                        detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().
                detectLeakedSqlLiteObjects().detectLeakedClosableObjects().
                penaltyLog().penaltyDeath().build());
        setContentView(R.layout.activity_account_reset);
        sharedPref = this.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        user_id = sharedPref.getInt("user_id", -1);
        get_phonenum();
        Account = (TextView)findViewById(R.id.id_);
        Account.setText(user.getPhone());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account_reset, menu);
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

    public void get_phonenum() {
        String jsonString = "{" +
                "\"id\":" + user_id + "}";
        String message = RequestHandler.sendPostRequest("http://120.24.208.130:1501/user/get_information", jsonString);
        if (message.equals("false")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "无法获取数据，请检查网络连接",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "获取周围用户信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                user = gson.fromJson(message, User.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
