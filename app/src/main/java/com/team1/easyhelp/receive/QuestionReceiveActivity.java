package com.team1.easyhelp.receive;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Answer;
import com.team1.easyhelp.entity.Event;
import com.team1.easyhelp.receive.adapter.AnswerAdapter;
import com.team1.easyhelp.send.AnswerSendActivity;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuestionReceiveActivity extends AppCompatActivity {

    private int user_id;
    private Event question;
    private List<Answer> answerList = new ArrayList<>();
    private AnswerAdapter answerAdapter;

    private TextView titleTv;
    private TextView contentTv;
    private RecyclerView answerListView;
    private TextView FollowButton;
    private TextView commitButton;

    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_receive);

        initial();
        followQuestion();
        commitAnswer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_question_receive, menu);
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
        // 初始化question实体类
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            question = (Event) bundle.getSerializable("event");
        // 初始化用户相关信息
        user_id = getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        initialLayout();
        initialRecyclerAdapter();

        initialData();
    }

    private void initialLayout() {
        // 绑定UI控件
        answerListView = (RecyclerView) findViewById(R.id.answer_list_text);
        titleTv = (TextView) findViewById(R.id.event_title_text);
        contentTv = (TextView) findViewById(R.id.event_content_text);

        FollowButton = (TextView) findViewById(R.id.followText);
        commitButton = (TextView) findViewById(R.id.commitText);

    }

    private void checkIfFollowed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonString = "{\"id\":" + user_id + ",\"event_id\":" +
                        question.getEventId() + "}";
                String message = RequestHandler.sendPostRequest(
                        "http://120.24.208.130:1501/user/judge_sup", jsonString);

            }
        }).start();
    }

    // 初始化AnswerList的视图
    private void initialRecyclerAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        answerListView.setLayoutManager(layoutManager);

        answerAdapter = new AnswerAdapter(answerList);
        answerAdapter.setOnRecyclerViewListener(new AnswerListListener());
        answerListView.setAdapter(answerAdapter);
    }

    // 初始化数据源，并在数据初始化成功以后将其更新到ListView中去 (后台运行)
    private void initialData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = true;
                    int i = 0;
                    do {
                        if (!flag)
                            Thread.sleep(8000);
                        flag = getAnswerList();
                        if (i++ == 3)
                            break;
                    } while (!flag);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean getAnswerList() {
        String jsonString = "{\"event_id\":" + 185 + "}";
//        String jsonString = "{\"event_id\":" + question.getEventId() + "}";
        String message = RequestHandler.sendPostRequest(
                "http://120.24.208.130:1501/event/anslist", jsonString);
        if (message.equals("false")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(QuestionReceiveActivity.this, "无法获取数据，请检查网络连接",
                            Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        } else {
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QuestionReceiveActivity.this, "获取周围事件失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
                String jsonStringList = jO.getString("answer_list");
                answerList = gson.fromJson(jsonStringList, new TypeToken<List<Answer>>(){}
                        .getType());

                // 等待回答列表获取成功以后再使用其重新初始化Adapter
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initialRecyclerAdapter();
                    }
                });
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    // 关注该问题
    private void followQuestion() {
        View view = this.findViewById(R.id.follow);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String jsonString = "{\"id\":" + user_id +
                                ",\"event_id\":" + question.getEventId() +
                                ",\"operation\":1" +
                                "}";
                    }
                }).start();
            }
        });
        setTouchAnimation(view);
    }


    // 为问题添加回答
    private void commitAnswer() {
        View view = this.findViewById(R.id.commit);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(QuestionReceiveActivity.this, AnswerSendActivity.class);
                startActivity(it);
            }
        });
        setTouchAnimation(view);
    }

    // 设置按下按钮后阴影效果
    private void setTouchAnimation(final View myView) {
        myView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    myView.setBackground(getResources().
                            getDrawable(R.drawable.horizon_divider_pressed));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    myView.setBackground(getResources().
                            getDrawable(R.drawable.horizon_divider));
                }
                return false;
            }
        });
    }

    public class AnswerListListener implements AnswerAdapter.OnRecyclerViewListener {
        @Override
        public void onItemClick(int position) {
            Toast.makeText(QuestionReceiveActivity.this, "位置" + position,
                    Toast.LENGTH_SHORT).show();
        }

        // 设置列表中item长按后的触发事件
        @Override
        public boolean onItemLongClick(int position) {
            return true;
        }
    }
}
