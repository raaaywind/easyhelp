package com.team1.easyhelp.home.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.SupportMapFragment;
import com.team1.easyhelp.R;
import com.team1.easyhelp.home.adapter.TabPagerItem;
import com.team1.easyhelp.home.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ViewPagerFragment extends Fragment {

    private List<TabPagerItem> mTabs = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createTabPagerItem();
    }

    private void createTabPagerItem(){
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_help_view),
                HomeFragment.newInstance(getString(R.string.title_fragment_help_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_sos_view),
                HomeFragment.newInstance(getString(R.string.title_fragment_sos_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_question_view),
                HomeFragment.newInstance(getString(R.string.title_fragment_question_view))));
        mTabs.add(new TabPagerItem(getString(R.string.title_fragment_neighbors_view),
                NeighborFragment.newInstance(getString(R.string.title_fragment_neighbors_view))));

//        MapStatus ms = new MapStatus.Builder().overlook(-20).zoom(15).build();
//        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
//                .compassEnabled(false).zoomControlsEnabled(false);
//        SupportMapFragment map = SupportMapFragment.newInstance(bo);
//        mTabs.add(new TabPagerItem("Map", map));
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

        mViewPager.setOffscreenPageLimit(mTabs.size());
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), mTabs));
        TabLayout mSlidingTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSlidingTabLayout.setElevation(15);
        }
        mSlidingTabLayout.setupWithViewPager(mViewPager);
    }

}
