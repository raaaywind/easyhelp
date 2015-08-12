package com.team1.easyhelp.home.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.team1.easyhelp.R;
import com.team1.easyhelp.home.adapter.TabPagerItem;
import com.team1.easyhelp.home.adapter.ViewPagerAdapter;
import com.team1.easyhelp.send.HelpMapActivity;
import com.team1.easyhelp.send.QuestionSendActivity;
import com.team1.easyhelp.send.TransitionActivity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ViewPagerFragment extends Fragment {

    private List<TabPagerItem> mTabs = new ArrayList<>();

    private FloatingActionsMenu menuMultipleActions;
    private FloatingActionButton sosButton;
    private FloatingActionButton helpButton;
    private FloatingActionButton quesButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createTabPagerItem();
    }

    private void createTabPagerItem() {
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_neighbors_view),
                NeighborFragment.newInstance(getString(R.string.title_fragment_neighbors_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_help_view),
                HelpListFragment.newInstance(getString(R.string.title_fragment_help_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_sos_view),
                SOSListFragment.newInstance(getString(R.string.title_fragment_sos_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_question_view),
                QuestionListFragment.newInstance(getString(R.string.title_fragment_question_view))));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_pager, container, false);
        rootView.setLayoutParams(new ViewGroup
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        // 为ViewPager视图内添加适配器的页面内容
        mViewPager.setOffscreenPageLimit(mTabs.size());
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), mTabs));

        // 将顶部tabLayout与ViewPager视图关联
        TabLayout mSlidingTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSlidingTabLayout.setElevation(15);
        }
        mSlidingTabLayout.setupWithViewPager(mViewPager);

        // 初始化浮动按钮
        initialFAB();
    }

    // 初始化页面的浮动按钮，为其设置触发事件
    private void initialFAB() {
        menuMultipleActions = (FloatingActionsMenu) getActivity().findViewById(R.id.multiple_actions);
        sosButton = (FloatingActionButton) getActivity().findViewById(R.id.sendsos);
        helpButton = (FloatingActionButton) getActivity().findViewById(R.id.sendhelp);
        quesButton = (FloatingActionButton) getActivity().findViewById(R.id.sendquestion);

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.toggle();
                startActivity(new Intent(getActivity(), TransitionActivity.class));
            }
        });
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.toggle();
                startActivity(new Intent(getActivity(), HelpMapActivity.class));
            }
        });
        quesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.toggle();
                startActivity(new Intent(getActivity(), QuestionSendActivity.class));
            }
        });
    }

}
