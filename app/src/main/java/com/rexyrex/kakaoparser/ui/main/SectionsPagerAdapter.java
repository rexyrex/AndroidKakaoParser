package com.rexyrex.kakaoparser.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Fragments.ChatFrequencyFrag;
import com.rexyrex.kakaoparser.Fragments.GeneralStatsFrag;
import com.rexyrex.kakaoparser.Fragments.WordAnalyseFrag;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = new String[]{"분석 개요", "사람 분석", "단어 분석"};
    private final Context mContext;
    private final ChatData cd;

    public SectionsPagerAdapter(Context context, FragmentManager fm, ChatData cd) {
        super(fm);
        this.cd = cd;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch(position){
            case 0 :
                GeneralStatsFrag gsf = GeneralStatsFrag.newInstance(cd); return gsf;
            case 1 :
                ChatFrequencyFrag cff = ChatFrequencyFrag.newInstance(cd); return cff;
            case 2 :
                WordAnalyseFrag waf = WordAnalyseFrag.newInstance(cd); return waf;

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