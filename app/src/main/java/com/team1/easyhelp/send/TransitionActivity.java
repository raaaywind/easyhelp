package com.team1.easyhelp.send;

import android.app.Service;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.team1.easyhelp.R;

/**
 * 控制发送求救之前的倒数动画
 */
public class TransitionActivity extends AppCompatActivity {
    // 动画视图控制
    private AnimatedCircleLoadingView animatedCircleLoadingView;

    private Vibrator vibrator;
    private Button cancelButton;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1850);
                for (int i = 3; i >= 1; i--) {
                    changePercent(i);// 将后台执行结果显示到UI
                    Thread.sleep(2600);
                }
                Thread.sleep(1000);
                // 如果该页面未被销毁，则跳转到求救页面
                if (!TransitionActivity.this.isFinishing()) {
                    getIntoSOS(); // 跳转到求救页面
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        initial();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vibrator.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transition, menu);
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

    public void sosCancel(View view) {
        TransitionActivity.this.finish();
    }

    private void initial() {
        initialButtonAnimation();

        animatedCircleLoadingView = (AnimatedCircleLoadingView) findViewById(
                R.id.circle_loading_view);
        startLoading();
        startPercentMockThread();

        vibratorStart();
    }

    private void vibratorStart() {
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        //依次为停顿时长，震动时长，停顿时长，震动时长 ...
        long[] pattern = {1850, 1400, 1200, 1400, 1200, 1400, 1200};
        vibrator.vibrate(pattern, -1);
    }

    // 设定Button的触摸响应动画
    private void initialButtonAnimation() {
        cancelButton = (Button) findViewById(R.id.cancel);

        cancelButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cancelButton.setBackground(getResources().
                            getDrawable(R.drawable.circle_animation_button_actiondown_shape));
                } else {
                    cancelButton.setBackground(getResources().
                            getDrawable(R.drawable.circle_animation_button_actionup_shape));
                }
                return false;
            }
        });
    }


    private void startLoading() {
        animatedCircleLoadingView.startDeterminate();
    }

    private void startPercentMockThread() {
        new Thread(runnable).start();
    }

    // 将后台执行结果显示到UI
    private void changePercent(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animatedCircleLoadingView.setPercent(percent);
            }
        });
    }

    // 跳转到求救页面
    private void getIntoSOS() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(TransitionActivity.this, SOSMapActivity.class);
                startActivity(intent);
                TransitionActivity.this.finish();
            }
        });
    }
}
