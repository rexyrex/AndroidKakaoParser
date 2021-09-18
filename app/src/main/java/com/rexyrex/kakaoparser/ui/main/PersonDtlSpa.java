package com.rexyrex.kakaoparser.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Fragments.main.ChatAnalyseFragment;
import com.rexyrex.kakaoparser.Fragments.main.GeneralStatsFrag;
import com.rexyrex.kakaoparser.Fragments.main.PersonAnalyseFrag;
import com.rexyrex.kakaoparser.Fragments.main.TimeAnalyseFrag;
import com.rexyrex.kakaoparser.Fragments.main.WordAnalyseFrag;
import com.rexyrex.kakaoparser.Fragments.person.PGeneralFrag;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PersonDtlSpa extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = new String[]{"개요", "단어", "시간"};
    private final Context mContext;
    private final ChatData cd;

    public PersonDtlSpa(Context context, FragmentManager fm) {
        super(fm);
        this.cd = ChatData.getInstance(context);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).


        switch(TAB_TITLES[position]){
            case "개요" :
                return PGeneralFrag.newInstance();
            case "단어" :
                return PGeneralFrag.newInstance();
            case "시간" :
                return PGeneralFrag.newInstance();
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