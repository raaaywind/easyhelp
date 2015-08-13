package com.team1.easyhelp.receive.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Answer;

import java.util.List;

/**
 * Created by thetruthmyg on 2015/8/13.
 */
public class AnswerAdapter extends RecyclerView.Adapter {

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

    private List<Answer> answerList;

    // 默认构造函数
    public AnswerAdapter(List<Answer> list) {
        // 初始化的时候只需要提供要渲染的EventList
        this.answerList = list;
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

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.answer_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new AnswerViewHolder(view);
    }


    // 通过ViewHolder将数据渲染到相应的itemView中去
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        AnswerViewHolder holder = (AnswerViewHolder) viewHolder;
        holder.position = i; // 绑定每个ViewItem对应的holder
        Answer answer = answerList.get(i);
        if (answer.getAuthor() == null) {
            holder.nicknameTv.setText("匿名用户");
        } else {
            holder.nicknameTv.setText(answer.getAuthor());
        }
        holder.dateTv.setText(answer.getTime());
        holder.contentTv.setText(answer.getContent());
        if (answer.getIs_adopted() == 0) {
            Drawable dr = holder.rootView.getBackground();
            holder.adoptedTv.setBackground(dr);
            holder.adoptedTv.setText("未采纳");
        }
    }

    // 获取Adapter当中Item的数目
    @Override
    public int getItemCount() {
        return answerList.size();
    }


    // 设置事件的ViewHolder的类，作为每一个Item视图的实体，可修改相应控件显示的内容
    class AnswerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public View rootView;
        public TextView nicknameTv;
        public TextView dateTv;
        public TextView contentTv;
        public TextView adoptedTv; //显示回答是否被采纳，未被采纳则背景色为空，透明显示
        public int position;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            // 绑定itemView当中的UI控件
            nicknameTv = (TextView) itemView.findViewById(R.id.nickname);
            dateTv = (TextView) itemView.findViewById(R.id.edit_date);
            contentTv = (TextView) itemView.findViewById(R.id.event_answer);
            adoptedTv = (TextView) itemView.findViewById(R.id.is_adopted_text);
            rootView = itemView.findViewById(R.id.answer_list_item); // 定义该Item的根视图

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
