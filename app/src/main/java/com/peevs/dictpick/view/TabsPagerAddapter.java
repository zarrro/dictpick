package com.peevs.dictpick.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.peevs.dictpick.model.TestQuestion;

/**
 * Created by zarrro on 01.01.16.
 */
public class TabsPagerAddapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "DictTab", "Quiz", "Word Books" };
    private Context context;
    private TestQuestion questionFromNotification;

    public TabsPagerAddapter(FragmentManager fm, Context context,
                             TestQuestion questionFromNotification) {
        super(fm);
        this.context = context;
        this.questionFromNotification = questionFromNotification;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DictTab();
            case 1:
                ExamTab et = new ExamTab();
                et.setNotificationQuestion(questionFromNotification);
                return et;
            case 2:
                return new WordsbookTab();
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
