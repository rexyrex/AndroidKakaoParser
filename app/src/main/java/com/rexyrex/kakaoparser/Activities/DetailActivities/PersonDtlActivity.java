package com.rexyrex.kakaoparser.Activities.DetailActivities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.ui.main.PersonDtlSpa;

public class PersonDtlActivity extends AppCompatActivity {

    PersonDtlSpa sectionsPagerAdapter;

    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_dtl);

        viewPager = findViewById(R.id.viewPagerPersonDtl);
        tabs = findViewById(R.id.tabsPersonDtl);
        titleTV = findViewById(R.id.titlePersonDtl);

        sectionsPagerAdapter = new PersonDtlSpa(PersonDtlActivity.this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

    }
}