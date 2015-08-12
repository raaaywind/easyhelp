package com.team1.easyhelp.home.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Event;
import com.team1.easyhelp.home.adapter.EventAdapter;
import com.team1.easyhelp.utils.ACache;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SOSListFragment extends Fragment {

    private static final int REFRESH_COMPLETE = 2;
    private static final String SOS_FRAGMENT = "SOS_FRAGMENT";
    private Context context;
    private View rootView;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView sosListView;

    private EventAdapter sosAdapter;

    private ACache eventCache; // 事件的缓存
    private int user_id;
    private List<Event> events = new ArrayList<>();

    private Gson gson = new Gson();


    public static SOSListFragment newInstance(String text) {
        SOSListFragment fragment = new SOSListFragment();
        Bundle args = new Bundle();
        args.putString(SOS_FRAGMENT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_soslist, container, false);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        context = rootView.getContext();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initial();
    }

    private void initial() {
        // 初始化用户相关信息
        user_id = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);

        initialLayout();
        // 填装数据
        initialData();
    }

    private void initialLayout() {
        // 绑定UI控件
        sosListView = (RecyclerView) rootView.findViewById(R.id.soslist);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.id_swipe_ly);

        // 使视图保持固定的大小
        sosListView.setHasFixedSize(true);

//        mSwipeLayout.setOnRefreshListener(this);
//        mSwipeLayout.setColorScheme(android.R.color.holo_green_dark, android.R.color.holo_green_light,
//                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    // 初始化视图的数据源
    private void initialData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNearbyEvents();
            }
        }).start();
    }

    private void initialRecyclerAdapter() {
        // 设置RecyclerView的LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        sosListView.setLayoutManager(layoutManager);

        sosAdapter = new EventAdapter(events);
        sosAdapter.setOnRecyclerViewListener(new EventListListener());
        sosListView.setAdapter(sosAdapter);
    }

//    // 响应下拉刷新事件
//    @Override
//    public void onRefresh()
//    {
//        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
//    }
//
//    private Handler mHandler = new Handler()
//    {
//        public void handleMessage(android.os.Message msg)
//        {
//            switch (msg.what)
//            {
//                case REFRESH_COMPLETE:
//                    eventCache = sosAdapter.getRemoteTitleList(0);
//                    setList(); // change the data in listview
//                    mSwipeLayout.setRefreshing(false);
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };


    // 获取周围发生的事件
    private void getNearbyEvents() {
        String jsonString = "{" +
                "\"id\":" + user_id +
                ",\"type\":2" +
                ",\"state\":0" + "}";
        String message = RequestHandler.sendPostRequest(
                "http://120.24.208.130:1501/event/get_nearby_event", jsonString);
        if (message.equals("false")) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "无法获取数据，请检查网络连接",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "获取周围事件失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                String jsonStringList = jO.getString("event_list");
                events = gson.fromJson(jsonStringList, new TypeToken<List<Event>>(){}
                        .getType());
                // 等待事件获取成功以后再使用其初始化Adapter
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initialRecyclerAdapter();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public class EventListListener implements EventAdapter.OnRecyclerViewListener {
        // 设置列表中item点击后的触发事件
        @Override
        public void onItemClick(int position) {
            Toast.makeText(context, "位置" + position,
                    Toast.LENGTH_SHORT).show();
        }

        // 设置列表中item长按后的触发事件
        @Override
        public boolean onItemLongClick(int position) {
            return true;
        }
    }

    public class MySwipeRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
    public void onRefresh() {}
    }

}
