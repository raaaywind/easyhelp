package com.team1.easyhelp.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Event;

import java.util.List;

/**
 * Created by thetruthmyg on 2015/8/12.
 */
public class EventAdapter extends RecyclerView.Adapter {

    // 定义监听事件的接口，以便在使用该类时重构监听事件
    public static interface OnRecyclerViewListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    // 设置该Adapter类的监听器
    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

//    private static final String TAG = EventAdapter.class.getSimpleName();
    private List<Event> eventList;

    // 默认构造函数
    public EventAdapter(List<Event> list) {
        // 初始化的时候只需要提供要渲染的EventList
        this.eventList = list;
    }

    /**
     * 以下三个函数实现了RecyclerView.Adapter中的三个方法：
     * - onCreateViewHolder()
     * - onBindViewHolder()
     * - getItemCount()
     */

    // 主要生成为每个Item去inflater(分配)一个View， 既为每一个Item提供ViewPager当中的一个显示位置
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new EventViewHolder(view);
    }

    // 通过ViewHolder将数据渲染到相应的itemView中去
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        EventViewHolder holder = (EventViewHolder) viewHolder;
        holder.position = i; // 绑定每个ViewItem对应的holder
        Event event = eventList.get(i);
        holder.nicknameTv.setText(event.getLauncher());
        holder.dateTv.setText(event.getTime());
        holder.coinTv.setText("爱心币: " + Integer.toString(event.getLove_coin()));
        holder.titleTv.setText(event.getTitle());
    }

    // 获取Adapter当中Item的数目
    @Override
    public int getItemCount() {
        return eventList.size();
    }


    // 设置事件的ViewHolder的类，作为每一个Item视图的实体，可修改相应控件显示的内容
    class EventViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public View rootView;
        public TextView nicknameTv;
        public TextView dateTv;
        public TextView coinTv;
        public TextView titleTv;
        public int position;

        public EventViewHolder(View itemView) {
            super(itemView);
            // 绑定itemView当中的UI控件
            nicknameTv = (TextView) itemView.findViewById(R.id.nickname);
            dateTv = (TextView) itemView.findViewById(R.id.edit_date);
            coinTv = (TextView) itemView.findViewById(R.id.love_coin);
            titleTv = (TextView) itemView.findViewById(R.id.event_title);
            rootView = itemView.findViewById(R.id.event_list_item); // 定义该Item的根视图

            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(null != onRecyclerViewListener){
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }
}
