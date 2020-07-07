package com.rexyrex.kakaoparser.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Activities.ChatStatsTabActivity;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeneralStatsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralStatsFrag extends Fragment {


    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;

    public GeneralStatsFrag() {
        // Required empty public constructor
    }

    public static GeneralStatsFrag newInstance() {
        GeneralStatsFrag fragment = new GeneralStatsFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //cd = getArguments().getParcelable(ARG_PARAM1);
            cd = ChatData.getInstance();
            database = MainDatabase.getDatabase(getContext());
            chatLineDao = database.getChatLineDAO();
            wordDao = database.getWordDAO();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_stats, container, false);

        ListView generalStatsLV = view.findViewById(R.id.generalStatsLV);

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
        //String dateRange = dateFormat.format(chatLineDao.getStartDate()) + "~" + dateFormat.format(chatLineDao.getEndDate());

        ArrayList<StringStringPair> pairs = new ArrayList<>();
        //pairs.add(new StringStringPair("분석 기간", "" + dateRange));
        pairs.add(new StringStringPair("대화 참여 인원", ""+ chatLineDao.getChatterCount()));
        pairs.add(new StringStringPair("분석 일 수", "" + StringParseUtils.numberCommaFormat(chatLineDao.getDayCount()+"")));
        pairs.add(new StringStringPair("분석 대화 수", "" + StringParseUtils.numberCommaFormat(chatLineDao.getCount()+"")));
        pairs.add(new StringStringPair("분석 단어 수", "" + StringParseUtils.numberCommaFormat(wordDao.getDistinctCount()+"")));
        pairs.add(new StringStringPair("분석 소요 시간 (초)", "" + cd.getLoadElapsedSeconds()));

        CustomAdapter customAdapter = new CustomAdapter(pairs);
        generalStatsLV.setAdapter(customAdapter);

        return view;
    }

    class CustomAdapter extends BaseAdapter {

        ArrayList<StringStringPair> pairs;

        CustomAdapter(ArrayList<StringStringPair> pairs){
            this.pairs = pairs;
        }

        @Override
        public int getCount() {
            return pairs.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_general_stat, null);

            TextView titleTV = convertView.findViewById(R.id.generalStatsElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.generalStatsElemValueTV);

            titleTV.setText(pairs.get(position).getTitle());
            valueTV.setText(pairs.get(position).getValue());

            if(pairs.get(position).getValue().length() > 12){
                valueTV.setTextSize(30);
            }

            return convertView;
        }
    }
}