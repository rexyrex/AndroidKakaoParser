package com.rexyrex.kakaoparser.Fragments.person;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PGeneralFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PGeneralFrag extends Fragment {


    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;

    NumberFormat numberFormat;

    public PGeneralFrag() {
        // Required empty public constructor
    }

    public static PGeneralFrag newInstance() {
        PGeneralFrag fragment = new PGeneralFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_p_general, container, false);


        return view;
    }

}