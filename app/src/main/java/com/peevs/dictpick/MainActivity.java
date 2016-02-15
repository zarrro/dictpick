package com.peevs.dictpick;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import android.view.Menu;
import android.widget.Toolbar;

import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.view.TabsPagerAdapter;

public class MainActivity extends TabFragmentHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        Intent i = getIntent();
        TestQuestion question = null;
        if (i != null) {
            question = (TestQuestion) i.getParcelableExtra(
                    NotificationPublisher.QUESTION_FROM_NOTIFICATION);
        }
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabsPagerAdapter(getFragmentManager(), this, question));

        if(question != null) {
            // if the activity is started with question from a notification start on the ExamTab
            viewPager.setCurrentItem(1);
        }

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
