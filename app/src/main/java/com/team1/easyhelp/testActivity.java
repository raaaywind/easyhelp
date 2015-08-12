package com.team1.easyhelp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.team1.easyhelp.home.HomeActivity;
import com.team1.easyhelp.receive.HelpReceiveMapActivity;
import com.team1.easyhelp.receive.QuestionReceiveActivity;
import com.team1.easyhelp.send.HelpMapActivity;
import com.team1.easyhelp.send.QuestionSendActivity;
import com.team1.easyhelp.send.SOSMapActivity;
import com.team1.easyhelp.send.TransitionActivity;

public class testActivity extends AppCompatActivity {

    private FloatingActionsMenu menuMultipleActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    public void getMap(View view) {
        startActivity(new Intent(this, SOSMapActivity.class));
    }

    public void getMap2(View view) {
        menuMultipleActions.toggle();
        startActivity(new Intent(this, TransitionActivity.class));
    }

    public void getSendQue(View view) {
        menuMultipleActions.toggle();
        startActivity(new Intent(this, QuestionSendActivity.class));
    }

    public void getSendHelp(View view) {
        menuMultipleActions.toggle();
        startActivity(new Intent(this, HelpMapActivity.class));
    }

    public void getHome(View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }

    public void viewQuestion(View view) {
        startActivity(new Intent(this, QuestionReceiveActivity.class));
    }
}
