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

        initialRecyclerAdapter();
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
                try {
                    boolean flag = true;
                    do {
                        if (!flag)
                            Thread.sleep(8000); // 若联网失败，则每过8秒重新联网一次
                        flag = getNearbyEvents();
                    } while (!flag);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    // 获取周围发生的事件
    private boolean getNearbyEvents() {
        String jsonString = "{" +
                "\"id\":" + user_id +
                ",\"type\":2" +
                ",\"state\":0" + "}";
        String message = RequestHandler.sendPostRequest(
                "http://120.24.208.130:1501/event/get_nearby_event", jsonString);
        // 暂时拿一堆假数据做测试
//        String message = "{\"status\": 200, \"event_list\": [{\"comment\": null, \"love_coin\": 0, \"title\": \"helppppppp\", \"event_id\": 292, \"follow_number\": 0, \"support_number\": 0, \"longitude\": 113.394203, \"content\": null, \"state\": 0, \"launcher_id\": 24, \"location\": null, \"time\": \"2015-08-04 16:42:19\", \"latitude\": 23.181321, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-08-04 22:35:40\", \"group_pts\": 0.0}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 291, \"follow_number\": 1, \"support_number\": 0, \"longitude\": 113.394203, \"content\": null, \"state\": 0, \"launcher_id\": 24, \"location\": null, \"time\": \"2015-08-04 15:51:51\", \"latitude\": 23.201321, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-08-04 22:35:40\", \"group_pts\": 0.0}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 241, \"follow_number\": 1, \"support_number\": 0, \"longitude\": 113.394203, \"content\": null, \"state\": 0, \"launcher_id\": 24, \"location\": null, \"time\": \"2015-08-04 15:50:35\", \"latitude\": 23.101321, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-08-04 16:39:25\", \"group_pts\": 0.0}, {\"title\": \"sos\", \"event_id\": 219, \"follow_number\": 0, \"longitude\": 113.399994, \"content\": null, \"state\": 0, \"launcher_id\": 21, \"time\": \"2015-07-31 16:33:21\", \"latitude\": 23.070954, \"type\": 2, \"last_time\": \"2015-07-31 16:33:21\"}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 218, \"follow_number\": 0, \"support_number\": 0, \"longitude\": 113.39359, \"content\": null, \"launcher\": \"18620730866\", \"state\": 0, \"launcher_id\": 21, \"location\": null, \"time\": \"2015-07-31 15:53:33\", \"latitude\": 23.072123, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-07-31 15:53:34\", \"group_pts\": 0.0}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 215, \"follow_number\": 0, \"support_number\": 1, \"longitude\": 113.393558, \"content\": null, \"launcher\": \"18620730866\", \"state\": 0, \"launcher_id\": 21, \"location\": null, \"time\": \"2015-07-31 15:53:15\", \"latitude\": 23.072134, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-07-31 15:53:15\", \"group_pts\": 0.0}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 206, \"follow_number\": 0, \"support_number\": 1, \"longitude\": 113.393559, \"content\": null, \"launcher\": \"18620730866\", \"state\": 0, \"launcher_id\": 21, \"location\": null, \"time\": \"2015-07-31 15:49:08\", \"latitude\": 23.072129, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-07-31 15:49:08\", \"group_pts\": 0.0}, {\"comment\": null, \"love_coin\": 0, \"title\": \"sos\", \"event_id\": 206, \"follow_number\": 0, \"support_number\": 1, \"longitude\": 113.398225, \"content\": null, \"launcher\": \"18620730866\", \"state\": 0, \"launcher_id\": 21, \"location\": null, \"time\": \"2015-07-31 15:30:25\", \"latitude\": 23.066616, \"demand_number\": 0, \"type\": 2, \"last_time\": \"2015-07-31 15:30:25\", \"group_pts\": 0.0}]}";
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
                    return false;
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
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
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
