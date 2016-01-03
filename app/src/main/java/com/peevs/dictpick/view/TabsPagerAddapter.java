package com.peevs.dictpick.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by zarrro on 01.01.16.
 */
public class TabsPagerAddapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Dict", "Quiz", "Word Books" };
    private Context context;

    public TabsPagerAddapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Dict();
            case 1:
                return new Exam();
            case 2:
                return new WordsBook();
            default:
                throw new IllegalArgumentException("tap page position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
