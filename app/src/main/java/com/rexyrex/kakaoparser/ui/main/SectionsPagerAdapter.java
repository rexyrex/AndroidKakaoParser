package com.rexyrex.kakaoparser.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Fragments.ChatAnalyseFragment;
import com.rexyrex.kakaoparser.Fragments.PersonAnalyseFrag;
import com.rexyrex.kakaoparser.Fragments.GeneralStatsFrag;
import com.rexyrex.kakaoparser.Fragments.TimeAnalyseFrag;
import com.rexyrex.kakaoparser.Fragments.WordAnalyseFrag;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = new String[]{"개요", "사람", "대화", "단어", "시간"};
    private final Context mContext;
    private final ChatData cd;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.cd = ChatData.getInstance();
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).


        switch(TAB_TITLES[position]){
            case "개요" :
                GeneralStatsFrag gsf = GeneralStatsFrag.newInstance(); return gsf;
            case "사람" :
                PersonAnalyseFrag cff = PersonAnalyseFrag.newInstance(); return cff;
            case "대화" :
                ChatAnalyseFragment caf = ChatAnalyseFragment.newInstance(); return caf;
            case "단어" :
                WordAnalyseFrag waf = WordAnalyseFrag.newInstance(); return waf;
            case "시간" :
                TimeAnalyseFrag taf = TimeAnalyseFrag.newInstance(); return taf;
            default : return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return mContext.getResources().getString(TAB_TITLES[position]);
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}