package com.example.newsfeed;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MyViewPagerAdapter extends FragmentPagerAdapter {
    private FragmentManager fm;
    private final List<Fragment> mFragments = new ArrayList<>();//添加的Fragment的集合
    private final List<String> mFragmentsTitles = new ArrayList<>();//每个Fragment对应的title的集合
//    private final List<String> mCategory = new ArrayList<>();
    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }
    /**
     * @param fragment      添加Fragment
     * @param fragmentTitle Fragment的标题，即TabLayout中对应Tab的标题
     */
    public void addFragment(Fragment fragment, String fragmentTitle) {
        mFragments.add(fragment);
        mFragmentsTitles.add(fragmentTitle);
//        mCategory.add(category);
    }

    @Override
    public Fragment getItem(int position) {
        //得到对应position的Fragment
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        //返回Fragment的数量
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //得到对应position的Fragment的title
        return mFragmentsTitles.get(position);
    }


    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container,
                position);
        fm.beginTransaction().show(fragment).commit();
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container, position, object);
        Fragment fragment = mFragments.get(position);
        fm.beginTransaction().hide(fragment).commit();
    }
}
