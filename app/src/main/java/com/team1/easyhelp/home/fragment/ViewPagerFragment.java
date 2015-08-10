package com.team1.easyhelp.home.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team1.easyhelp.R;
import com.team1.easyhelp.home.adapter.TabPagerItem;
import com.team1.easyhelp.home.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ViewPagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private List<TabPagerItem> mTabs = new ArrayList<>();

//
//    public ViewPagerFragment() {
//        // Required empty public constructor
//    }

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



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
