package com.team1.easyhelp.home.adapter;

import android.support.v4.app.Fragment;

/**
 * Created by thetruthmyg on 2015/8/10.
 * 定义Fragment的类，包含Fragment视图与其标题
 */
public class TabPagerItem {
    private final CharSequence mTitle;
    private final Fragment mFragment;

    public TabPagerItem(CharSequence title, Fragment fragment) {
        this.mTitle = title;
        this.mFragment = fragment;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public CharSequence getTitle() {
        return mTitle;
    }
}
