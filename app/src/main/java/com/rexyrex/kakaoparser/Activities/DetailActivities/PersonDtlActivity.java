package com.rexyrex.kakaoparser.Activities.DetailActivities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.ui.main.PersonDtlSpa;

public class PersonDtlActivity extends AppCompatActivity {

    PersonDtlSpa sectionsPagerAdapter;

    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;
    SharedPrefUtils spu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_dtl);

        spu = new SharedPrefUtils(this);

        viewPager = findViewById(R.id.viewPagerPersonDtl);
        tabs = findViewById(R.id.tabsPersonDtl);
        titleTV = findViewById(R.id.titlePersonDtl);

        titleTV.setText(generateTitleSpannableText("사람 분석 : " + spu.getString(R.string.SP_PERSON_DTL_NAME, "사람 정보"), spu.getString(R.string.SP_CHAT_DT_RANGE_STRING, "")));

        sectionsPagerAdapter = new PersonDtlSpa(PersonDtlActivity.this, getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

    }

    private SpannableString generateTitleSpannableText(String title, String dateRangeStr) {
        SpannableString s = new SpannableString(title + "\n" + dateRangeStr);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.lightBrown)), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.ITALIC), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(15, true), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //AlignmentSpan alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
        //s.setSpan(alignmentSpan, title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }
}